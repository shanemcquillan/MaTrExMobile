package ie.cngl.dcu;

import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

//Used to make remote procedure call to Matrex service
public class MatrexClient {
    private String serviceURL;

    private XmlRpcClient client = null;

    private Base64 base64 = new Base64();

    public MatrexClient(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public void init() throws Exception {
        client = new XmlRpcClient();
        XmlRpcCommonsTransportFactory transportFactory = new XmlRpcCommonsTransportFactory( client );
        client.setTransportFactory( transportFactory );
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        URL serverURL = new URL(serviceURL);
        config.setServerURL(serverURL);
        client.setConfig(config);
    }

    public void destroy() {

    }

    public String translate(String input) throws Exception {
        String result = (String) client.execute("translate",
                new Object[] { base64.encodeToString(input.getBytes()) });
        return new String(base64.decodeBase64(result.getBytes()), "UTF-8");
    }
}
