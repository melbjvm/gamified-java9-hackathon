package io.r3k.hackathon.hvcjava9.controllers;

import java.security.Principal;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubmitController {

    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(SubmitController.class);

    private static final Map<String, AtomicInteger> stepThreeHashMap = new TreeMap<>();

    private static final Map<String, UserScore> score = new TreeMap<>();

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public SubmitController(SimpMessagingTemplate template, InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.template = template;
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    private SimpMessagingTemplate template;

    @RequestMapping(method = RequestMethod.GET, value = "go")
    public String hello() {
        log.info("hello");
        return "Hello Hackathon ";
    }

    @RequestMapping(method = RequestMethod.POST, value = "go/uploadjavaversion")
    public SubmitResult step1UploadJavaVersion(@RequestParam("source") String input, Principal principal) {
        log.info("step1UploadJavaVersion " + input + " " + principal.getName());
        if (input.matches("(?s).*9-ea\\+1[2-9]\\d.*")) {
            getScore(principal.getName()).setStep1(1);
            sendUpdate();
            return new SubmitResult(true, null, "/jshellchallenge.html");
        }
        if (input.contains("java version")) {
            return new SubmitResult(false, "The wrong version of Java is submitted", null);
        }
        return new SubmitResult(false, "This does not seem to be the output of java -version", null);
    }

    void sendUpdate() throws MessagingException {
        //template.convertAndSendToUser("user1", "/user/queue/horray", new Greeting("Only for you, " + principal.getName() + "!"));
        //this.template.convertAndSend("/topic/greetings", new Greeting("manula " + System.currentTimeMillis()));
    }

    @RequestMapping(path = "go/{user}/jshellput", method = RequestMethod.PUT)
    public String step2(@PathVariable(value = "user") String user, HttpServletRequest request/*,@RequestHeader  HttpHeaders headers*/) {
        log.info("step2 " + user);
        if (!inMemoryUserDetailsManager.userExists(user)) {
            return "Unknown user, check your user name in the URL you specifed";
        }
        String output = "Well done. Now perform 50 HTTP POST to the URL " + request.getRequestURL().toString();
        getScore(user).setStep2(1);
        sendUpdate();
        return output.replace("jshellput", "jshellpost");
    }

    @RequestMapping(path = "go/{user}/jshellpost", method = RequestMethod.POST)
    public String step3(@PathVariable(value = "user") String user, HttpServletRequest request) {
        log.info("step3 " + user);
        if (!inMemoryUserDetailsManager.userExists(user)) {
            return "Unknown user, check your user name in the URL you specifed";
        }
        AtomicInteger userCount = stepThreeHashMap.get(user);
        if (userCount == null) {
            userCount = new AtomicInteger();
            stepThreeHashMap.put(user, userCount);
        }
        if (userCount.get() < 50) {
            return "Do the same another " + (50 - userCount.incrementAndGet()) + " times";
        }
        getScore(user).setStep3(1);
        sendUpdate();
        return "Well done. Using your browser, browse to the page https://j9.melbjvm.com/letsencrypt.html";
    }

    @RequestMapping(path = "go/{user}/letsencrypt", method = RequestMethod.PUT)
    public String step4(@PathVariable(value = "user") String user, HttpServletRequest request) {
        log.info("step4 " + user);
        if (!inMemoryUserDetailsManager.userExists(user)) {
            return "Unknown user, check your user name in the URL you specifed";
        }
        if (request.getRequestURL().toString().toLowerCase().contains("https://")) {
            getScore(user).setStep4(1);
            sendUpdate();
            return "Well done. Using your browser, browse to the page https://j9.melbjvm.com/jep286.html";
        }
        return "Sorry, but you need to use https and not http. Try again.";
    }

    @RequestMapping(path = "scores", method = RequestMethod.GET)
    public AllScores getAllScores() {
        log.info("getAllScores ");
        return new AllScores(score);
    }

    void setAllScores() {
        score.clear();
    }

    @RequestMapping(path = "scores.csv", method = RequestMethod.GET)
    public String getAllScoresCSV() {
        log.info("getAllScores.csv");
        StringBuilder csv = new StringBuilder();
        csv.append("Name,Score\n");
        for (String name : score.keySet()) {
            csv.append(name).append(",").append(score.get(name).getTotal()).append("\n");
        }
        return csv.toString();
    }
    @RequestMapping(value = "register/getallusers", method = RequestMethod.GET)
    public String getAllUsers() {
        StringBuilder b = new StringBuilder();
        for (String name : score.keySet()) {
            UserDetails up = inMemoryUserDetailsManager.loadUserByUsername(name);
            b.append(up.getUsername()).append(",").append(up.getPassword()).append("\n");
        }
        return b.toString();
    }
    
    public static UserScore getScore(String user) {
        UserScore userScore = score.get(user);
        if (userScore == null) {
            userScore = new UserScore();
            score.put(user, userScore);
        }
        return userScore;
    }

    @RequestMapping("setscore/{step}/{user}/{value}")
    public String setStepScore(@PathVariable("step") int step, @PathVariable("user") String user, @PathVariable("value") int value, HttpServletRequest httpServletRequest) {
        if (!inMemoryUserDetailsManager.userExists(user)) {
            return "Unknown user, check your user name in the URL you specifed";
        }
        UserScore userScore = getScore(user);
        switch (step) {
            case 1:
                userScore.setStep1(value);
                break;
            case 2:
                userScore.setStep2(value);
                break;
            case 3:
                userScore.setStep3(value);
                break;
            case 4:
                userScore.setStep4(value);
                break;
            case 5:
                userScore.setStep5(value);
                break;
            case 6:
                userScore.setStep6(value);
                break;
        }
        sendUpdate();
        return "done " + userScore.toString();
    }

    @RequestMapping(method = RequestMethod.POST, value = "go/")
    public String currentUser(@RequestParam("source") String input, Principal principal) {
        log.info("currentUser " + input + " " + principal.getName());
        UserDetails up = inMemoryUserDetailsManager.loadUserByUsername(principal.getName());
        return principal.getName() + " - score :"+getScore(principal.getName()).getTotal() + " pass :"+up.getPassword();
    }

    
}
