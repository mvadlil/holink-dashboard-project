package com.example.holink.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.holink.entity.ClickEvent;

public interface ClickEventRepository extends JpaRepository<ClickEvent, String> {

    @Query("""
            select ce.link.id as linkId, count(ce.id) as totalClicks
            from ClickEvent ce
            where ce.link.id in :linkIds
            group by ce.link.id
            """)
    List<LinkClickCountView> countClicksGroupedByLinkIds(@Param("linkIds") Collection<String> linkIds);
}
