package hello;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.directory.DirContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    //ldap template
    @Autowired
    private LdapTemplate ldapTemplate;

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {

        String userDn = "uid=guest3,ou=users,ou=guests,dc=zflexsoftware,dc=com";//username + ldapDomainName;
        //token expiration time 4 hours
        Date tokenExpired = new Date(new Date().getTime() + 60 * 60 * 4 * 1000);
        DirContext ctx = null;
        try {
            //Authenticate domain users with username and password
            ctx = ldapTemplate.getContextSource().getContext(userDn, "guest3password");
            //If validation succeeds, query the user name and the group to which the user belongs based on the sAMAccountName attribute
//            Employee employee = ldapTemplate                                                        .search(query().where("objectclass").is("person").and("sAMAccountName").is(username),
//                    new EmployeeAttributesMapper())
//                    .get(0);
            //Encryption of User Name and Group Information with Jwt
//            String compactJws = Jwts.builder()
//                    .setSubject(employee.getName())
//                    .setAudience(employee.getRole())
//                    .setExpiration(tokenExpired)
//                    .signWith(SignatureAlgorithm.HS512, jwtKey).compact();
            //The login is successful and the client token information is returned. Only user names and roles are encrypted here, while displayName and token Expired are not encrypted.
            Map<String, Object> userInfo = new HashMap<String, Object>();
//            userInfo.put("token", compactJws);
//            userInfo.put("displayName", employee.getDisplayName());
            userInfo.put("tokenExpired", tokenExpired.getTime());

            return new Greeting(HttpStatus.OK.value(),
                    JSON.toJSONString(userInfo, SerializerFeature.DisableCircularReferenceDetect));
        } catch (Exception e) {
            //Logon failure, return failed HTTP status code
            System.out.println(e);
            return new Greeting(HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase());
        } finally {
            //Close ldap connection
            LdapUtils.closeContext(ctx);
        }
    }
}
