package ei1034.votoElectronico.votoElectronico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomepageController {

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }
}
