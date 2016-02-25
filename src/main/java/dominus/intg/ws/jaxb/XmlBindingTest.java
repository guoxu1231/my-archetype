package dominus.intg.ws.jaxb;


import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.StringReader;


/**
 * EE: Contract First
 * When using contract-first, you start with the WSDL contract, and use Java to implement said contract.
 * 1, XML Document;
 * 2, Generate XSD schema from XML document;
 */
public class XmlBindingTest extends TestCase {

    String personXml;

    @Override
    protected void setUp() throws Exception {
        personXml = IOUtils.toString(new File("archetype-helloworld/src/main/java/dominus/intg/ws/jaxb/xml/Person.xml").toURI(), "UTF-8");
        System.out.println("[JUnit].setUp/n" + personXml);
    }

    public void testSimpleUnmarshal() {
        Person person = JAXB.unmarshal(new StringReader(personXml), Person.class);
        assertEquals(person.getFirstName(), "Shawn");
        assertEquals(person.getLastName(), "Guo");
    }

    //TODO schema validation, multiple xml body, complex xml
    //Contract-First Web Services


}
