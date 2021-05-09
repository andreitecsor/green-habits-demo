package eco.collectio.service;

import eco.collectio.domain.Follow;
import eco.collectio.domain.Post;
import eco.collectio.domain.PostType;
import eco.collectio.domain.User;
import eco.collectio.exception.InvalidPostException;
import eco.collectio.repository.FollowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FollowService {
    private final FollowRepository followRepository;
    private final UserService userService;
    private final PostService postService;
    private Logger logger = LoggerFactory.getLogger(FollowService.class);

    @Autowired
    public FollowService(FollowRepository followRepository, UserService userService, PostService postService) {
        this.followRepository = followRepository;
        this.userService = userService;
        this.postService = postService;
    }

    public Follow upsert(Long idUserWhoFollows, Long idUserWhoIsFollowed) {
        Follow result = followRepository.findByNodesIds(idUserWhoFollows, idUserWhoIsFollowed);
        if (result == null) {
            Optional<User> userWhoFollows = userService.getById(idUserWhoFollows);
            Optional<User> userWhoIsFollowed = userService.getById(idUserWhoIsFollowed);
            if (!userWhoFollows.isPresent() || !userWhoIsFollowed.isPresent()) {
                logger.error("user who follows(id=" + idUserWhoFollows + ") or user whoIsFollowed(id=" + idUserWhoIsFollowed + ") does not exists");
                return null;
            }
            Follow newFollowRelation = new Follow(userWhoFollows.get(), userWhoIsFollowed.get());
            try {
                createFollowPost(newFollowRelation.getUserWhoFollows(), newFollowRelation.getUserWhoIsFollowed());
                return followRepository.save(newFollowRelation);
            } catch (InvalidPostException e) {
                e.printStackTrace();
            }
        }

        Follow followUpdate = followUpdate(result);
        if (followUpdate != null) {
            return followUpdate;
        }
        logger.error(result.toString() + " is still active");
        return null;
    }

    private void createFollowPost(User userWhoFollows, User userWhoIsFollowed) throws InvalidPostException {
        postService.upsert(new Post.PostBuilder(PostType.FOLLOW, userWhoFollows)
                .setFollowing(userWhoIsFollowed)
                .build());
    }

    private Follow followUpdate(Follow result) {
        if (result.getLastTimeUnfollowed() != null) {
            try {
                result.followAgain();
                createFollowPost(result.getUserWhoFollows(), result.getUserWhoIsFollowed());
                return followRepository.save(result);
            } catch (InvalidPostException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Follow unfollow(Long idUserWhoFollows, Long idUserWhoIsFollowed) {
        Follow result = followRepository.findByNodesIds(idUserWhoFollows, idUserWhoIsFollowed);
        if (result == null || result.getLastTimeUnfollowed() != null) {
            logger.error("The specific FOLLOWS relation does not exists or it's already ended");
            return null;
        }

        Optional<Post> optionalPost = postService.getPostByFollowerIdAndFollowingId(idUserWhoFollows, idUserWhoIsFollowed);
        if (!optionalPost.isPresent()) {
            return null;
        }
        Post followPost = optionalPost.get();
        followPost.makeUnavailable();
        postService.upsert(followPost);
        result.unfollow();
        return followRepository.save(result);
    }
}
