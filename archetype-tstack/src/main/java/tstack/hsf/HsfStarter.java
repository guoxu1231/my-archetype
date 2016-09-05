package tstack.hsf;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.taobao.hsf.standalone.HSFEasyStarter;

public class HsfStarter {
    public static void main(String[] args) throws InterruptedException {
        HSFEasyStarter.startFromPath("/opt/Development/github_repo/my-archetype/archetype-tstack/lib");
        Thread.sleep(3000l);
        // 初始化Spring容器
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/application.xml");
    }
}
