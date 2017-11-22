package dominus.spec;


import io.swagger.models.Path;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;
import io.swagger.parser.SwaggerParser;
import io.swagger.models.Swagger;

import static org.junit.Assert.assertEquals;

public class TestSwaggerParser extends DominusJUnit4TestBase {

    @Test
    public void testSpecParser() {
        Swagger swagger = new SwaggerParser().read("http://petstore.swagger.io/v2/swagger.json");
        assertEquals("petstore.swagger.io", swagger.getHost());
        assertEquals(6, swagger.getDefinitions().size());
        assertEquals(3, swagger.getTags().size());
        for (String path : swagger.getPaths().keySet())
            out.println(path);
    }


}
