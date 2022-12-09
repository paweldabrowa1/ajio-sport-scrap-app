package cmd.map;

import cmd.Command;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.TextSearchRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.example.db.map.MapTableDao;
import org.example.db.map.PlayingField;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapCommand extends Command {

    //	const PARAMS_REGEXP = 'query(?:\\((.*?)\\))?\\{(.*?)(?:\\((.*?)\\))?\\{';
    //((.*),)?\s?([0-9]{2}-[0-9]{3})\s(.*)?
    Pattern addressPattern = Pattern.compile("((.*),)?\\s?([0-9]{2}-[0-9]{3})\\s(.*)?", Pattern.DOTALL);

    public MapCommand(MapTableDao mapTable) {
        super("map", mapTable);
    }

    @Override
    public void onSetup(Options options) {
        Option loops = new Option(
                "l", "loops",
                true, "number of stadiums downloaded pages (20 records per page)"
        );
        loops.setRequired(true);
        options.addOption(loops);

        Option input = new Option(
                "lat",
                true, "start map latitude"
        );
        options.addOption(input);

        Option output = new Option(
                "lng",
                true, "start map longitude"
        );
        options.addOption(output);
    }

    @Override
    public void onExecute(CommandLine cmd) {
        String startLat = cmd.getOptionValue("lat");
        String startLng = cmd.getOptionValue("lng");

        double lat = 50.06952107813107;
        double lng = 19.938121663862745;

        if (startLat != null) {
            try {
                lat = Double.parseDouble(startLat);
            } catch (Exception e) {
                System.out.print("'lat' is not a number\n");

                System.exit(1);
            }
        }
        if (startLng != null) {
            try {
                lng = Double.parseDouble(startLng);
            } catch (Exception e) {
                System.out.print("'lat' is not a number\n");

                System.exit(1);
            }
        }
        LatLng location = new LatLng(lat, lng);

        int loops = 0;
        String aLoops = cmd.getOptionValue("loops");
        try {
            loops = Integer.parseInt(aLoops);
        } catch (Exception e) {
            System.out.print("'loops' is not a number\n");

            System.exit(1);
        }
        if (loops < 1) loops = 1;

        String apiKey = "AIzaSyAb3d3uMAZVM2jpDxtDi3GVF67FK7IT0eo";
        GeoApiContext ctx = new GeoApiContext.Builder().apiKey(apiKey).build();

        try {
            PlacesSearchResponse resp = PlacesApi.textSearchQuery(ctx, PlaceType.STADIUM)
                    .location(location)
                    .radius(2000)
                    .await();

            handlePlaces(resp.results);

            if (loops > 1) {

                String npToken = resp.nextPageToken;

                for (int i = 1; i < loops; i++) {
                    Thread.sleep(5000);

                    TextSearchRequest request = new TextSearchRequest(ctx);
                    request.pageToken(npToken);
                    PlacesSearchResponse respNext = request.await();

                    handlePlaces(respNext.results);

                    npToken = respNext.nextPageToken;

                    if (npToken == null) {
                        System.out.printf("--- No more pages to download. Finishing at: %d page\n", i + 1);

                        System.exit(1);
                    }
                }
            }
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    void handlePlaces(PlacesSearchResult[] results) {
        for (PlacesSearchResult r : results) {
            if (!Objects.equals(r.businessStatus, "OPERATIONAL")) continue;
            printPlace(r);
        }
    }

    void printPlace(PlacesSearchResult r) {
        LatLng loc = r.geometry.location;

        String street = "", postalCode = "", city = "";
        String error = null;

        Matcher m = addressPattern.matcher(r.formattedAddress);
        if (m.find()) {
            street = m.group(2);
            postalCode = m.group(3);
            city = m.group(4);
        } else {
            error = "NO_REGEX_APPLIED";
        }

        getMapTable().insertField(new PlayingField(
                r.name,
                street,
                postalCode,
                city,
                loc.lat,
                loc.lng,
                error,
                r.formattedAddress
        ));
    }
}
