package dominus.spec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

public class TestJJWT extends DominusJUnit4TestBase {

    // We need a signing key, so we'll create one just for this example. Usually
    // the key would be read from your application configuration instead.
    Key key = MacProvider.generateKey(SignatureAlgorithm.HS256);

    @Test
    public void testSignAndVerifyToken() {
        out.printf("Algorithm:%s\nString Representation:%s\nFormat:%s\n", key.getAlgorithm(), new String(key.getEncoded()), key.getFormat());

        //EE:Sets the JWT's payload to be a plaintext (non-JSON) string
        String compactJws1 = Jwts.builder()
                .setPayload("hello world")
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        out.println("JWT:" + compactJws1);
        assert Jwts.parser().setSigningKey(key).parsePlaintextJws(compactJws1).getBody().equals("hello world");
        out.println(Jwts.parser().setSigningKey(key).parsePlaintextJws(compactJws1));

        //EE:Sets the JWT payload to be a JSON Claims instance populated by the specified name/value pairs.
        Map session = new HashMap<String,String>();
        session.put("user-id","shawguo");
        session.put("roles","admin,op,user");
        String compactJws2 = Jwts.builder()
                .setSubject("Joe")
                .setClaims(session)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        out.println(Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws2));
        assert Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws2).getBody().get("user-id").equals("shawguo");
        assert Jwts.parser().setSigningKey(key).parseClaimsJws(compactJws2).getBody().get("roles").equals("admin,op,user");
    }


}
