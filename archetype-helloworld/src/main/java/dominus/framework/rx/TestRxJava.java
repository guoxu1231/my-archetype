package dominus.framework.rx;


import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;
import rx.Observable;

public class TestRxJava extends DominusJUnit4TestBase {

    @Test
    public void testCreateObservable() {
        Observable<String> o = Observable.from(new String[]{"a", "b", "c"});
        o.subscribe(System.out::println);
    }

}
