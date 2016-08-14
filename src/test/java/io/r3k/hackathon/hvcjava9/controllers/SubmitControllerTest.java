package io.r3k.hackathon.hvcjava9.controllers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.Principal;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@RunWith(MockitoJUnitRunner.class)
public class SubmitControllerTest {

    @Mock
    protected SimpMessagingTemplate mockSimpMessagingTemplate;

    @Mock
    protected InMemoryUserDetailsManager inMemoryUserDetailsManager;
    
    @Test
    public void shouldDetectUploadJava9VersionForStep1() {
        //given
        String input = "java version \"9-ea\"\n"
                + "Java(TM) SE Runtime Environment (build 9-ea+120)\n"
                + "Java HotSpot(TM) 64-Bit Server VM (build 9-ea+120, mixed mode)";
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertTrue(result.isSuccess());
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep1());

    }

    @Test
    public void shouldDetectUploadJava9VersionForStep1WithMinimalInput() {
        //given
        String input = "9-ea+120";
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertTrue(result.isSuccess());
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep1());
    }

    @Test
    public void shouldDetectUploadJava9VersionForStep1WithMinimalInputWith12xVersion() {
        //given
        String input = "9-ea+125";
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        final SubmitController submitController = new SubmitController(mock(SimpMessagingTemplate.class),inMemoryUserDetailsManager);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertTrue(result.isSuccess());
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep1());
    }

    @Test
    public void shouldDetectUploadJava9VersionForStep1WithMinimalInputWith13xVersion() {
        //given
        String input = "9-ea+131";
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        final SubmitController submitController = new SubmitController(mock(SimpMessagingTemplate.class),inMemoryUserDetailsManager);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertTrue(result.isSuccess());
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep1());
    }

    @Test
    public void shouldNotDetectJava9AsItsJava8() {
        //given
        String input = "java version \"1.8.0_45\"\n"
                + "Java(TM) SE Runtime Environment (build 1.8.0_45-b14)\n"
                + "Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)";
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "The wrong version of Java is submitted");
        UserScore score = submitController.getScore(user);
        assertEquals(0, score.getStep1());

    }

    @Test
    public void shouldNotDetectJava9AsItsNotJava() {
        //given
        String input = "Python 2.7.11+";
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        Principal userPrincipal = mock(Principal.class);
        String user = UUID.randomUUID().toString();
        when(userPrincipal.getName()).thenReturn(user);
        //when
        SubmitResult result = submitController.step1UploadJavaVersion(input, userPrincipal);
        //then
        assertFalse(result.isSuccess());
        assertEquals(result.getMessage(), "This does not seem to be the output of java -version");
        UserScore score = submitController.getScore(user);
        assertEquals(0, score.getStep1());
    }

    @Test
    public void shouldReturnResultURLWhenPutCalled() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "http://jozijugdojo.co.za/go/" + user + "/jshellput";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        String result = submitController.step2(user, httpServletRequest);
        //then
        assertEquals("Well done. Now perform 50 HTTP POST to the URL http://jozijugdojo.co.za/go/" + user + "/jshellpost", result);
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep2());
    }
    @Test
    public void shouldReturnResultURLWhenPutCalledAndEchoRequestURL() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "http://someurl.com/go/" + user + "/jshellput";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        String result = submitController.step2(user, httpServletRequest);
        //then
        assertEquals("Well done. Now perform 50 HTTP POST to the URL http://someurl.com/go/" + user + "/jshellpost", result);
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep2());
    }
        @Test
    public void shouldReturnResultUnknownUser() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(false);
        String requestURL = "http://jozijugdojo.co.za/go/" + user + "/jshellput";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        String result = submitController.step2(user, httpServletRequest);
        //then
        assertEquals("Unknown user, check your user name in the URL you specifed", result);
        UserScore score = submitController.getScore(user);
        assertEquals(0, score.getStep2());
    }
    
    @Test
    public void shouldReturnRunAnotherxTimesAndThenHTTPSURL() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "http://jozijugdojo.co.za/go/" + user + "/jshellpost";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        for (int count = 0; count < 50; count++) {

            //when
            String result = submitController.step3(user, httpServletRequest);
            //then
            assertEquals(result, "Do the same another " + (50 - count - 1) + " times");
        }
        String result = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).step3(user, httpServletRequest);
        assertEquals("Well done. Using your browser, browse to the page https://jozijugdojo.co.za/letsencrypt.html", result);
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep3());
    }

    @Test
    public void shouldReturnJEP286AfterDoingSucessfulHTTPS() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "HTTPS://JOZIJUGDOJO.co.za/GO/" + user + "/letsencrypt";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        String result = submitController.step4(user, httpServletRequest);
        //then
        assertEquals("Well done. Using your browser, browse to the page https://jozijugdojo.co.za/jep286.html", result);
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep4());
    }

    @Test
    public void shouldReturnJEP286AfterDoingSucessfulhttps() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "https://jozijugdojo.co.za/go/" + user + "/letsencrypt";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        //when
        String result = submitController.step4(user, httpServletRequest);
        //then
        assertEquals("Well done. Using your browser, browse to the page https://jozijugdojo.co.za/jep286.html", result);
        UserScore score = submitController.getScore(user);
        assertEquals(1, score.getStep4());
    }

    @Test
    public void shouldNotReturnJEP286AfterDoingSucessfulHTTP() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        String requestURL = "http://localhost:8080/go/" + user + "/letsencrypt";
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer(requestURL));
        //when
        String result = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).step4(user, httpServletRequest);
        //then
        assertEquals("Sorry, but you need to use https and not http. Try again.", result);
    }

    @Test
    public void shouldSetScoreManuallyStep1() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 1;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep1());
        assertEquals(1, score.getTotal());
    }

    @Test
    public void shouldSetScoreManuallyStep2() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 2;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep2());
        assertEquals(1, score.getTotal());
    }

    @Test
    public void shouldSetScoreManuallyStep3() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 3;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep3());
        assertEquals(1, score.getTotal());
    }

    @Test
    public void shouldSetScoreManuallyStep4() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 4;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep4());
        assertEquals(1, score.getTotal());
    }

    @Test
    public void shouldSetScoreManuallyStep5() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 5;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep5());
        assertEquals(1, score.getTotal());
    }

    @Test
    public void shouldSetScoreManuallyStep6() {
        //given
        String user = UUID.randomUUID().toString();
        when(inMemoryUserDetailsManager.userExists(user)).thenReturn(true);
        int step = 6;
        //when
        shouldSetScoreManually(user, step);
        //then
        UserScore score = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).getScore(user);
        assertEquals(1, score.getStep6());
        assertEquals(1, score.getTotal());
    }

    private void shouldSetScoreManually(String user, int step) {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        Principal userPrincipal = mock(Principal.class);

        when(userPrincipal.getName()).thenReturn(user);

        //when
        String result = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager).setStepScore(step, user, 1, httpServletRequest);
    }

    @Test
    public void shouldGetScoresCSV() {
        // given
        final SubmitController submitController = new SubmitController(mockSimpMessagingTemplate,inMemoryUserDetailsManager);
        String user1 = UUID.randomUUID().toString();
        String user2 = UUID.randomUUID().toString();
        submitController.getScore(user1).setStep1(1);
        submitController.getScore(user1).setStep2(1);
        submitController.getScore(user2);
        // when
        String CSV = submitController.getAllScoresCSV();
        // then
        Stream<String> lines = new BufferedReader(new StringReader(CSV)).lines();
        Object[] toArray = lines.toArray();
        assertEquals("Name,Score",toArray[0]);
//        assertEquals(user1+",2",toArray[1]);
//        assertEquals(user1+",2",toArray[1]);
        
    }
    
}
