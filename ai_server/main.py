# main.py
from fastapi import FastAPI
from pydantic import BaseModel
import os
import yaml
import hashlib
import random
from openai import OpenAI

app = FastAPI()

# =========================
# 경로 설정
# =========================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

SPRING_YML_PATH = os.path.normpath(
    os.path.join(BASE_DIR, "..", "src", "main", "resources", "application.yml")
)

DATA_DIR = os.path.join(BASE_DIR, "data")

# =========================
# Spring application.yml에서 OpenAI 키 읽기
# =========================
def load_openai_key_from_spring_yml(yml_path: str) -> str:
    if not os.path.exists(yml_path):
        raise RuntimeError(f"Spring application.yml 파일을 찾을 수 없습니다: {yml_path}")

    with open(yml_path, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f)

    if not isinstance(data, dict):
        raise RuntimeError("application.yml 파싱 결과가 dict가 아닙니다.")

    openai_cfg = data.get("openai", {})
    if isinstance(openai_cfg, dict):
        if isinstance(openai_cfg.get("api-key"), str):
            return openai_cfg["api-key"].strip()
        if isinstance(openai_cfg.get("api"), dict):
            if isinstance(openai_cfg["api"].get("key"), str):
                return openai_cfg["api"]["key"].strip()

    if isinstance(data.get("openai.api.key"), str):
        return data["openai.api.key"].strip()

    raise RuntimeError("application.yml에서 OpenAI 키를 찾지 못했습니다.")


OPENAI_API_KEY = load_openai_key_from_spring_yml(SPRING_YML_PATH)
client = OpenAI(api_key=OPENAI_API_KEY)

# =========================
# 요청/응답 모델
# =========================
class HotelChatRequest(BaseModel):
    hotelExtId: str
    message: str


class HotelChatResponse(BaseModel):
    reply: str


# =========================
# 파일 로딩
# =========================
def read_text(path: str) -> str:
    if not os.path.exists(path):
        return ""
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


# =========================
# 언어 감지
# =========================
def detect_language(text: str) -> str:
    for ch in text:
        if "가" <= ch <= "힣":
            return "Korean"
    return "English"


# =========================
# 호텔 타입 판별
# =========================
def detect_hotel_type(hotel_ext_id_or_name: str) -> str:
    s = (hotel_ext_id_or_name or "").lower()

    if any(k in s for k in ["bike", "cycle", "자전거"]):
        return "bike_friendly"
    if any(k in s for k in ["grand", "palace", "plaza", "resort", "lux", "hotel", "호텔"]):
        return "luxury"
    if any(k in s for k in ["business", "biz", "비즈니스"]):
        return "business"
    if any(k in s for k in ["guesthouse", "hostel", "inn", "게스트", "모텔", "motel"]):
        return "budget"

    return "default"


# =========================
# ✅ 호텔별 "고정 정책" 생성 (seed + 타입 가중치)
# - 서버 재시작해도 동일하게 나오도록 hotelExtId로 seed 고정
# - bike_friendly면 자전거 보관 정책이 더 "친화적으로" 뽑히도록 가중치
# =========================
def _rng_for_hotel(hotel_ext_id: str) -> random.Random:
    seed = int(hashlib.sha256((hotel_ext_id or "").encode("utf-8")).hexdigest(), 16)
    return random.Random(seed)


def _weighted_choice(rng: random.Random, weight_map: dict) -> str:
    # weight_map: {"key": 0.4, "key2": 0.6}
    items = list(weight_map.items())
    keys, weights = zip(*items)
    return rng.choices(keys, weights=weights, k=1)[0]


# 정책 문장(영어) 후보: 호텔별로 다르게 섞이게끔 여러 버전 준비
POLICY_CANDIDATES = {
    "bicycle": {
        "room": [
            "Bicycles can be stored inside the guest room if you use a cover/mat to protect the floor.",
            "Guests may keep bicycles in their rooms, provided the bicycle is kept clean and does not damage the room."
        ],
        "indoor_common": [
            "The hotel provides an indoor common storage area for bicycles.",
            "There is a designated indoor bicycle storage space available for guests."
        ],
        "outdoor_area": [
            "A designated outdoor area is available for bicycle storage (lock recommended).",
            "Bicycle storage is available in an outdoor designated spot; please use your own lock."
        ],
        "nearby_storage": [
            "If indoor storage is not available, you can use a nearby bicycle parking/storage facility.",
            "If the hotel cannot store bicycles indoors, a nearby storage option is available."
        ],
    },
    "luggage": {
        "standard": [
            "Luggage storage is generally available before check-in and after check-out at the front desk.",
            "You can usually leave your luggage at the front desk before check-in and after check-out, depending on availability."
        ]
    },
    "food": {
        "light_ok": [
            "Light meals such as convenience food, takeout, and delivery are generally allowed in rooms.",
            "Convenience-store food and delivery meals are typically okay in the room."
        ],
        "no_cooking": [
            "Cooking that produces strong odors or smoke (e.g., grilling meat, boiling stew) is not permitted in rooms.",
            "Please avoid cooking with strong smells or smoke in the room (e.g., grilling or boiling stew)."
        ]
    },
    "parking": {
        "onsite": [
            "On-site parking may be available; please check availability with the front desk.",
            "Parking is available on-site in many cases, but availability may be limited—please confirm with the front desk."
        ],
        "nearby": [
            "If on-site parking is limited, a nearby public parking facility can be used.",
            "If the hotel parking is full, you can use a nearby public parking option."
        ]
    }
}

# 호텔 타입별 가중치 (bike_friendly만 자전거 쪽을 친화적으로)
POLICY_WEIGHTS = {
    "bike_friendly": {
        "bicycle": {
            "room": 0.45,
            "indoor_common": 0.35,
            "outdoor_area": 0.15,
            "nearby_storage": 0.05,
        }
    },
    "default": {
        "bicycle": {
            "room": 0.15,
            "indoor_common": 0.25,
            "outdoor_area": 0.30,
            "nearby_storage": 0.30,
        }
    },
    "business": {
        "bicycle": {
            "room": 0.10,
            "indoor_common": 0.20,
            "outdoor_area": 0.30,
            "nearby_storage": 0.40,
        }
    },
    "budget": {
        "bicycle": {
            "room": 0.05,
            "indoor_common": 0.15,
            "outdoor_area": 0.35,
            "nearby_storage": 0.45,
        }
    },
    "luxury": {
        "bicycle": {
            "room": 0.25,
            "indoor_common": 0.35,
            "outdoor_area": 0.20,
            "nearby_storage": 0.20,
        }
    }
}


def generate_hotel_policy_summary(hotel_ext_id: str) -> str:
    """
    ✅ 호텔별로 고정되는 정책 요약 텍스트를 생성한다.
    - seed: hotelExtId 기반 -> 서버 재시작해도 동일
    - bike_friendly 등 호텔 타입에 따라 bicycle 정책 가중치 적용
    """
    rng = _rng_for_hotel(hotel_ext_id)
    hotel_type = detect_hotel_type(hotel_ext_id)

    # 타입이 없으면 default로 처리
    type_weights = POLICY_WEIGHTS.get(hotel_type, POLICY_WEIGHTS["default"])

    lines = []

    # bicycle (가중치 선택)
    bicycle_weights = type_weights.get("bicycle", POLICY_WEIGHTS["default"]["bicycle"])
    bicycle_key = _weighted_choice(rng, bicycle_weights)
    lines.append(rng.choice(POLICY_CANDIDATES["bicycle"][bicycle_key]))

    # luggage (고정 후보 중 1개)
    lines.append(rng.choice(POLICY_CANDIDATES["luggage"]["standard"]))

    # food (light_ok + no_cooking 둘 다 넣어서 안내가 명확하게)
    lines.append(rng.choice(POLICY_CANDIDATES["food"]["light_ok"]))
    lines.append(rng.choice(POLICY_CANDIDATES["food"]["no_cooking"]))

    # parking (onsite vs nearby 랜덤 1개)
    parking_key = rng.choice(["onsite", "nearby"])
    lines.append(rng.choice(POLICY_CANDIDATES["parking"][parking_key]))

    return "\n".join(lines).strip()


# =========================
# RAG 컨텍스트 생성 (✅ 기존 로직 유지 + 정책 요약만 추가)
# =========================
def build_context(hotel_ext_id: str) -> str:
    base_text = read_text(os.path.join(DATA_DIR, "base.txt"))

    hotel_type = detect_hotel_type(hotel_ext_id)
    hotel_type_text = read_text(
        os.path.join(DATA_DIR, "hotel_type", f"{hotel_type}.txt")
    )

    facilities_dir = os.path.join(DATA_DIR, "facilities")
    facilities_texts = []
    if os.path.isdir(facilities_dir):
        for fname in sorted(os.listdir(facilities_dir)):
            if fname.endswith(".txt"):
                facilities_texts.append(
                    read_text(os.path.join(facilities_dir, fname))
                )

    # ✅ 추가: 호텔별 고정 정책(타입 가중치 반영)
    policy_summary = generate_hotel_policy_summary(hotel_ext_id)

    # ✅ 기존 구성(base + type + facilities) 그대로 + 맨 끝에 policy_summary만 붙임
    return "\n\n".join(
        [base_text, hotel_type_text] + facilities_texts + [
            "Hotel-specific policy summary (generated and fixed per hotel):",
            policy_summary
        ]
    ).strip()


# =========================
# 엔드포인트
# =========================
@app.post("/chat/hotel", response_model=HotelChatResponse)
def chat_hotel(req: HotelChatRequest):
    context = build_context(req.hotelExtId)
    lang = detect_language(req.message)

    language_instruction = (
        "Answer ONLY in English."
        if lang == "English"
        else "한국어로만 답변하세요."
    )

    system_prompt = f"""
You are an automated hotel information system.
You do not choose the response language.
You MUST follow the output language rule strictly.

LANGUAGE RULE (ABSOLUTE):
{language_instruction}
Do NOT mix languages.
Do NOT translate unless explicitly asked.

IMPORTANT CONTEXT RULE:
- Welcome or greeting messages are FIXED UI greetings.
- They do NOT indicate the language to respond in.
- Always determine response language ONLY from the user's latest message.

CONTEXT RULE:
- Context is for hotel information only.
- Context language does NOT determine response language.
- Treat "Hotel-specific policy summary" as the hotel's actual policy for this chat.

ANSWERING RULES:
- Use context if relevant.
- If information is missing, answer based on common hotel practices.
- Avoid absolute claims; use words like "generally", "typically", or "depending on the hotel".

Context:
{context}
""".strip()

    completion = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": req.message}
        ],
        temperature=0.3,
    )

    reply = completion.choices[0].message.content.strip()

    if not reply:
        reply = (
            "Sorry, I couldn't generate a response."
            if lang == "English"
            else "답변을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요."
        )

    return HotelChatResponse(reply=reply)
