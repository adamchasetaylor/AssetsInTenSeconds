import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

import com.twilio.Twilio;
import com.twilio.rest.serverless.v1.Service;
import com.twilio.rest.serverless.v1.service.Environment;
import com.twilio.rest.serverless.v1.service.asset.AssetVersion;
import com.twilio.rest.serverless.v1.service.Asset;
import com.twilio.rest.serverless.v1.service.Build;
import com.twilio.rest.serverless.v1.service.environment.Deployment;

import java.io.File;
import java.util.concurrent.TimeUnit;

class Main {

    public static final String TWILIO_ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    public static final String TWILIO_API_KEY = System.getenv("TWILIO_API_KEY");
    public static final String TWILIO_SECRET = System.getenv("TWILIO_SECRET");

    private static String createService() {
        Service service = Service.creator("my-new-app", "My New App").setIncludeCredentials(true).create();
        return service.getSid();
    }

    private static Environment createEnvironment(String service_sid) {
        Environment environment = Environment.creator(service_sid, "staging").setDomainSuffix("stage").create();
        return environment;
    }

    private static String createAsset(String service_sid) {
        Asset asset = Asset.creator(service_sid, "friendly_name").create();
        return asset.getSid();
    }

    private static String createBuild(String asset_version_sid, String service_sid) {
        Build build = Build.creator(service_sid)
            .setAssetVersions(asset_version_sid)
            .create();

        return build.getSid();
    }

    private static String createDeployment(String service_sid, String environment_sid, String build_sid) {
        Deployment deployment = Deployment.creator(service_sid, environment_sid)
            .setBuildSid(build_sid)
            .create();

        return deployment.getSid();
    }

    public static void main(String[] args) {
        // Setup Default Twilio Client
        Twilio.init(TWILIO_API_KEY, TWILIO_SECRET, TWILIO_ACCOUNT_SID);

        String service_sid = createService();
        Environment environment = createEnvironment(service_sid);
        String domain_name = environment.getDomainName();
        String environment_sid = environment.getSid();
        String asset_sid = createAsset(service_sid);

        // Create Asset Verion with UniRest
        String remote_path = "/hello.txt";
        String remote_visibility = "Public";
        File asset_file = new File("hello.txt");
        String asset_type = "text/plain";

        String url = "https://serverless-upload.twilio.com/v1/Services/" + service_sid + "/Assets/" + asset_sid + "/Versions";

        HttpResponse < JsonNode > response = Unirest.post(url)
            .basicAuth(TWILIO_API_KEY, TWILIO_SECRET)
            .field("Content", asset_file, asset_type)
            .field("Path", remote_path)
            .field("Visibility", remote_visibility)
            .charset(null)
            .asJson();

        JSONObject assetVersion = response.getBody().getObject();
        String asset_version_sid = assetVersion.get("sid").toString();

        String build_sid = createBuild(asset_version_sid, service_sid);
        System.out.println("Please wait 10-15 seconds while build completes...");

        // Sleep for 15 Seconds to allow build time to finish
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        createDeployment(service_sid, environment_sid, build_sid);

        System.out.println("http://" + domain_name + remote_path);

    }
}