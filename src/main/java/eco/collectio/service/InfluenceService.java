package eco.collectio.service;

import eco.collectio.domain.Influence;
import eco.collectio.domain.User;
import eco.collectio.repository.InfluenceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InfluenceService {
    private final InfluenceRepository influenceRepository;
    private final UserService userService;

    public InfluenceService(InfluenceRepository repository, UserService userService) {
        this.influenceRepository = repository;
        this.userService = userService;
    }

    /**
     * @return all INFLUENCED relationships from database
     */
    public List<Influence> get() {
        return influenceRepository.findAll();
    }

    /**
     * @param whoInfluencedId   of user who influenced other user to join a challenge
     * @param whoIsInfluencedId of user who was influenced
     * @return new or updated INFLUENCED relationship between the two users
     */
    public Influence upsert(Long whoInfluencedId, Long whoIsInfluencedId) {
        Influence result = influenceRepository.findByNodesIds(whoInfluencedId, whoIsInfluencedId);
        if (result == null) {
            Optional<User> whoInfluenced = userService.getById(whoInfluencedId);
            Optional<User> whoIsInfluenced = userService.getById(whoIsInfluencedId);
            if (!whoInfluenced.isPresent() || !whoIsInfluenced.isPresent()) {
                System.err.println("user or challenge does not exists");
                return null;
            }
            Influence newInfluenceRelation = new Influence(whoInfluenced.get(), whoIsInfluenced.get(), 1, LocalDateTime.now());
            return influenceRepository.save(newInfluenceRelation);
        }
        result.increaseInfluence();
        return influenceRepository.save(result);
    }
}
