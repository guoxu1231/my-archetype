package tstack;


import com.taobao.hsf.standalone.HSFEasyStarter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:spring/*.xml")
public class HsfApplication {


    public static void main(String[] args) throws InterruptedException {

        String hsfPath = "/opt/Development/github_repo/my-archetype/archetype-tstack/lib";
        HSFEasyStarter.startFromPath(hsfPath);
        Thread.sleep(5000l);
        SpringApplication.run(HsfApplication.class, args);
    }
}
