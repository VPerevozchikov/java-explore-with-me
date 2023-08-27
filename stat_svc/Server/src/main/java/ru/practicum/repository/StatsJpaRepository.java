package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsJpaRepository extends JpaRepository<EndpointHit, Integer>, JpaSpecificationExecutor<EndpointHit> {
    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(hit.ip)) " +
            "from EndpointHit as hit " +
            "where hit.timestamp between ?1 and ?2 " +
            "group by hit.uri, hit.app " +
            "order by count(hit.id) desc")
    List<ViewStats> getStatsNotUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(DISTINCT hit.ip)) " +
            "from EndpointHit as hit " +
            "where hit.timestamp between ?1 and ?2 " +
            "group by hit.uri, hit.app " +
            "order by count(hit.id) desc")
    List<ViewStats> getStatsUnique(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(hit.ip)) " +
            "from EndpointHit as hit " +
            "where hit.uri IN ?3 " +
            "and hit.timestamp between ?1 and ?2 " +
            "group by hit.uri, hit.app " +
            "order by count(hit.id) desc")
    List<ViewStats> getStatsNotUniqueWithUris(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("select new ru.practicum.dto.ViewStats(hit.app, hit.uri, count(DISTINCT hit.ip)) " +
            "from EndpointHit as hit " +
            "where hit.uri IN ?3 " +
            "and hit.timestamp between ?1 and ?2 " +
            "group by hit.uri, hit.app " +
            "order by count(hit.id) desc")
    List<ViewStats> getStatsUniqueWithUris(LocalDateTime start, LocalDateTime end, String[] uris);
}