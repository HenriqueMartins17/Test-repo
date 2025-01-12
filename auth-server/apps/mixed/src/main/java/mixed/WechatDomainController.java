package mixed;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author Leo Zhao
 * @date 2021/10/14 22:29
 */
@RestController
public class WechatDomainController {

    @RequestMapping("/8563929553.txt")
    @ResponseBody
    public String getWechatDomainCheckFile() {
        return "985c63ed1c867ed423451865e686aa7d";
    }
}
