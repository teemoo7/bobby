package ch.teemoo.bobby.lichess;

import java.util.stream.Stream;

import chariot.ClientAuth;
import chariot.model.Event;
import chariot.model.GameEvent;

public class LichessClient {

	private final ClientAuth client;

	public LichessClient(ClientAuth client) {
		this.client = client;
	}

	public Stream<Event> streamEvents() {
		return client.bot().connect().stream();
	}

	public void acceptChallenge(String challengeId) {
		client.bot().acceptChallenge(challengeId);
	}

	public void declineChallenge(String challengeId) {
		client.bot().declineChallenge(challengeId);
	}

	public Stream<GameEvent> streamGameEvents(String gameId) {
		return client.bot().connectToGame(gameId).stream();
	}

	public void move(String gameId, String moveUci) {
		client.bot().move(gameId, moveUci);
	}

	public void abort(String gameId) {
		client.bot().abort(gameId);
	}

	public void resign(String gameId) {
		client.bot().resign(gameId);;
	}

	public void chat(String gameId, String message) {
		client.bot().chat(gameId, message);
	}

}
