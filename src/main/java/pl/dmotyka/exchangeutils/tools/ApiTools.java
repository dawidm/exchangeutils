package pl.dmotyka.exchangeutils.tools;

import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.dmotyka.exchangeutils.exceptions.ConnectionProblemException;
import pl.dmotyka.exchangeutils.exceptions.ExchangeCommunicationException;

public class ApiTools {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getJsonNode(String url) throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            return objectMapper.readValue(new URL(url), JsonNode.class);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }

    public <T> T getDataObj(String url, Class<T> dataobjClass) throws ConnectionProblemException, ExchangeCommunicationException {
        try {
            return objectMapper.readValue(new URL(url), dataobjClass);
        } catch (UnknownHostException | SocketException e) {
            throw new ConnectionProblemException("when getting symbols", e);
        } catch (IOException e) {
            throw new ExchangeCommunicationException("when getting symbols", e);
        }
    }

}
