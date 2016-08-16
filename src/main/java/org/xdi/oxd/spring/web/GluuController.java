package org.xdi.oxd.spring.web;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xdi.oxd.common.response.GetTokensByCodeResponse;
import org.xdi.oxd.common.response.GetUserInfoResponse;
import org.xdi.oxd.spring.Settings;
import org.xdi.oxd.spring.security.AuthoritiesConstants;
import org.xdi.oxd.spring.security.GluuUser;
import org.xdi.oxd.spring.service.OxdService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/gluu")
public class GluuController {

    @Inject
    private OxdService oxdService;

    @Inject
    private Settings settings;

    @RequestMapping(path = "/redirect", method = RequestMethod.GET)
    public String redirect(@RequestParam(name = "code", required = false) String code,
                           @RequestParam(name = "error", required = false) String error,
                           @RequestParam(name = "error_description", required = false) String errorDescription,
                           RedirectAttributes redirectAttributes) {

        if (error != null) {
            redirectAttributes.addAttribute("error", error);
            redirectAttributes.addAttribute("error_description", errorDescription);
            return "redirect:/error";
        }

        Optional<GetTokensByCodeResponse> tokenResponse = Optional.of(oxdService)
                .map(c -> c.getTokenByCode(settings.getOxdId(), code))
                .map(c -> c.dataAsResponse(GetTokensByCodeResponse.class));
        GetUserInfoResponse userInfoResponse = tokenResponse
                .map(c -> oxdService.getUserInfo(settings.getOxdId(), c.getAccessToken()))
                .map(c -> c.dataAsResponse(GetUserInfoResponse.class))
                .orElseThrow(() -> new BadCredentialsException("Can't get user info"));

        Collection<GrantedAuthority> authorities = Arrays
                .asList(new GrantedAuthority[]{new SimpleGrantedAuthority(AuthoritiesConstants.USER)});

        GluuUser user = new GluuUser(tokenResponse.get().getIdToken(), userInfoResponse.getClaims(), authorities);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, "", authorities));

        return "redirect:/user";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/home";
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String handleAllException() {
        return "redirect:/error";
    }

}
