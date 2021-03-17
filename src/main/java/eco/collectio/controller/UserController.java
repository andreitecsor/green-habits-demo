package eco.collectio.controller;

import eco.collectio.domain.User;
import eco.collectio.service.JoinService;
import eco.collectio.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final JoinService joinService;

    public UserController(UserService userService, JoinService joinService) {
        this.userService = userService;
        this.joinService = joinService;
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getAll() {
        List<User> result = userService.getAll();
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("")
    public ResponseEntity<User> create(@RequestBody User user) {
        User result = userService.create(user);
        if (result == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/challenge/{challengeId}")
    public ResponseEntity<List<User>> getAllByChallenge(@PathVariable Long challengeId) {
        List<User> result = userService.getAllByChallenge(challengeId);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }
}