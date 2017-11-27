package dominus.dsl

import groovy.json.JsonSlurper
import groovy.xml.*
import org.junit.Test
import origin.common.junit.DominusJUnit4TestBase

class TestJson2XmlDSL extends DominusJUnit4TestBase {

    @Override
    protected void doSetUp() throws Exception {
    }


    @Test
    public void testJson2Xml() {
        def jsonSlurper = new JsonSlurper()

        //parse json to groovy map, http://groovy-lang.org/json.html
        def jsonObj = jsonSlurper.parse(new File(resourceLoader.getResource("classpath:data/json/batters.json").getURI()))
        assert jsonObj instanceof Map
        assert jsonObj.name == 'Cake'
        assert jsonObj.batters.batter.size == 4
        assert jsonObj.batters.batter[0].type == 'Regular'
        for (def batter : jsonObj.batters.batter)
            println batter.type

        //Creating XML, http://groovy-lang.org/processing-xml.html
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.batters {
            id(jsonObj.id)
            type(jsonObj.type)
            name(jsonObj.name)
            ppu(jsonObj.ppu)
            batters {
                jsonObj.batters.batter.each { bat ->
                    batter {
                        id(bat.id)
                        type(bat.type)
                    }
                }
            }
            topping {
                jsonObj.topping.each { top ->
                    id(top.id)
                    type(top.type)
                }
            }
        }
        println writer

    }

}
