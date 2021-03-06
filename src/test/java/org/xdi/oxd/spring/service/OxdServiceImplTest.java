package org.xdi.oxd.spring.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xdi.oxd.common.CommandResponse;
import org.xdi.oxd.common.response.GetAuthorizationUrlResponse;
import org.xdi.oxd.common.response.UpdateSiteResponse;
import org.xdi.oxd.spring.OxdSpringApplication;
import org.xdi.oxd.spring.Settings;

import javax.inject.Inject;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OxdSpringApplication.class)
public class OxdServiceImplTest {

    @Value("${oxd.client.callback-uri}")
    private String callbackUrl;

    @Inject
    private OxdService oxdService;

    @Inject
    private Settings settings;

    @Test
    public void updateSite() {
        CommandResponse cr = oxdService.updateSite(settings.getOxdId(), callbackUrl);
        Optional<UpdateSiteResponse> updateSiteResponse = Optional.of(cr)
                .map(c -> c.dataAsResponse(UpdateSiteResponse.class));
        Assert.assertTrue(updateSiteResponse.isPresent());
    }

    @Test
    public void getAuthorizationUrl() {
        Optional<GetAuthorizationUrlResponse> getAuthorizationUrlResponse = Optional
                .of(oxdService.getAuthorizationUrl(settings.getOxdId()))
                .map(c -> c.dataAsResponse(GetAuthorizationUrlResponse.class));
        Assert.assertTrue(getAuthorizationUrlResponse.isPresent());
    }
}
