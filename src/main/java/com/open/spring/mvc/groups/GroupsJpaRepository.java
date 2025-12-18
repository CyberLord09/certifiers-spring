package com.open.spring.mvc.groups;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.open.spring.mvc.person.Person;

public interface GroupsJpaRepository extends JpaRepository<Groups, Long> {
    
    // Find a group by its ID
    Optional<Groups> findById(Long id);
    
    // Find all groups
    List<Groups> findAll();
    List<Groups> findAllByOrderByNameAsc();

    // Eagerly load members to avoid LazyInitialization in views
    @Query("SELECT DISTINCT g FROM Groups g LEFT JOIN FETCH g.groupMembers gm ORDER BY g.id")
    List<Groups> findAllWithMembers();
    
    // Find groups containing a specific person by uid
    @Query("SELECT g FROM Groups g JOIN g.groupMembers p WHERE p.uid = :personUid")
    List<Groups> findGroupsByPersonUid(@Param("personUid") String personUid);
    
    // Find groups containing a specific person by id
    @Query("SELECT g FROM Groups g JOIN g.groupMembers p WHERE p.id = :personId")
    List<Groups> findGroupsByPersonId(@Param("personId") Long personId);

    @Query("SELECT DISTINCT g FROM Groups g LEFT JOIN FETCH g.groupMembers gm WHERE EXISTS (SELECT 1 FROM g.groupMembers p WHERE p.id = :personId) ORDER BY g.id")
    List<Groups> findGroupsByPersonIdWithMembers(@Param("personId") Long personId);
    
    // Find groups with a specific number of members
    @Query("SELECT g FROM Groups g WHERE SIZE(g.groupMembers) = :memberCount")
    List<Groups> findGroupsByMemberCount(@Param("memberCount") int memberCount);
}