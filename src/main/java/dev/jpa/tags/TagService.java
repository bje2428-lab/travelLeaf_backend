package dev.jpa.tags;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Transactional
public class TagService {


private final TagRepository tagRepository;


// Create
public TagDTO createTag(TagDTO dto) {
if (tagRepository.existsByName(dto.getName())) {
throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + dto.getName());
}


// tag_id는 DB 시퀀스에서 생성한다고 가정 (직접 할당)
// 만약 JPA에서 자동 생성하려면 @GeneratedValue(strategy = GenerationType.SEQUENCE...) 설정 필요
Tag tag = Tag.builder()
.tagId(null) // 시퀀스를 DB에서 nextval로 채우려면 네이티브 쿼리 또는 DB trigger 필요
.name(dto.getName())
.build();


Tag saved = tagRepository.save(tag);
return toDto(saved);
}


// Read all
public List<TagDTO> getAllTags() {
return tagRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
}

//Read by id
public TagDTO getTag(Long id) {
return tagRepository.findById(id).map(this::toDto)
.orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
}


//Search by keyword
public List<TagDTO> searchTags(String keyword) {
String kw = (keyword == null) ? "" : keyword.trim();
return tagRepository.findAll().stream()
.filter(t -> t.getName().contains(kw))
.map(this::toDto)
.collect(Collectors.toList());
}


//Update
public TagDTO updateTag(Long id, TagDTO dto) {
Tag tag = tagRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));


if (!tag.getName().equals(dto.getName()) && tagRepository.existsByName(dto.getName())) {
throw new IllegalArgumentException("이미 존재하는 태그 이름입니다: " + dto.getName());
}


tag.setName(dto.getName());
Tag saved = tagRepository.save(tag);
return toDto(saved);
}


//Delete
public void deleteTag(Long id) {
if (!tagRepository.existsById(id)) {
throw new IllegalArgumentException("Tag not found: " + id);
}
tagRepository.deleteById(id);
}


//Mapper
private TagDTO toDto(Tag t) {
return TagDTO.builder()
.tagId(t.getTagId())
.name(t.getName())
.build();
}

//🔥 자동완성 (DB에서 like 검색)
public List<TagDTO> autocomplete(String keyword) {
 String kw = (keyword == null) ? "" : keyword.trim();
 if (kw.isEmpty()) return List.of();

 return tagRepository.findByNameContainingIgnoreCase(kw).stream()
         .map(this::toDto)
         .collect(Collectors.toList());
}
public List<TagPopularDTO> getPopularTags(int limit) {

  return tagRepository.findPopularTags(
          PageRequest.of(0, limit)
  ).stream()
   .map(obj -> {
       Tag tag = (Tag) obj[0];
       Long cnt = (Long) obj[1];
       return new TagPopularDTO(
               tag.getTagId(),
               tag.getName(),
               cnt
       );
   })
   .toList();
}

}