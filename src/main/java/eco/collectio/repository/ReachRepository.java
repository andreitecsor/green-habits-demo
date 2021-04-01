package eco.collectio.repository;

import eco.collectio.domain.Reach;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReachRepository extends Neo4jRepository<Reach, Long> {

    @Query("MATCH (user:User)-[relation:REACHED]->(stage:Stage) " +
            "WHERE id(user) = $userId AND id(stage) = $stageId " +
            "RETURN user,stage,relation")
    Reach findByNodesIds(Long userId, Long stageId);

    @Query("MATCH (user:User)-[relation:REACHED]->(stage:Stage)<-[:HAS]-(challenge:Challenge) " +
            "WHERE id(user)=$userId AND id(challenge)= $challengeId AND relation.show = true " +
            "SET relation.show = false " +
            "RETURN user,relation,stage")
    Reach hideActiveBadgeFromChallenge(Long userId, Long challengeId);

}