// [START create_channel_1]
public class TicTacToeServlet extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		// Game creation, user sign-in, etc. omitted for brevity.
		String userId = userService.getCurrentUser().getUserId();

		ChannelService channelService = ChannelServiceFactory.getChannelService();

		// The 'Game' object exposes a method which creates a unique string based on the game's key
		// and the user's id.
		String token = channelService.createChannel(game.getChannelKey(userId));

		// Index is the contents of our index.html resource, details omitted for brevity.
		index = index.replaceAll("\\{\\{ token \\}\\}", token);

		resp.setContentType("text/html");
		resp.getWriter().write(index);
	}
}
// [END create_channel_1]

// [START create_channel_2]
<body>
<script>
channel = new goog.appengine.Channel('{{ token }}');
socket = channel.open();
socket.onopen = onOpened;
socket.onmessage = onMessage;
socket.onerror = onError;
socket.onclose = onClose;
</script>
</body>
// [END create_channel_2]

// [START validate_message]
public class Game {
	// member variables, etc omitted for brevity.

	public String getChannelKey(String user) {
		return user + KeyFactory.keyToString(key);
	}

	private void sendUpdateToUser(String user) {
		if (user != null) {
			ChannelService channelService = ChannelServiceFactory.getChannelService();
			String channelKey = getChannelKey(user);
			channelService.sendMessage(new ChannelMessage(channelKey, getMessageString()));
		}
	}

	public void sendUpdateToClients() {
		sendUpdateToUser(userX);
		sendUpdateToUser(userO);
	}
}

public class MoveServlet extends HttpServlet {
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String gameId = req.getParameter("g");
		Game game = pm.getObjectById(Game.class, KeyFactory.stringToKey(gameId));

		// Code to retrieve user id, check rules and update game omitted for brevity
		game.sendUpdateToClients();
	}
}
// [END validate_message]

// [START tracking_client_connections_and_disconnections]
<inbound-services>
<service>channel_presence</service>
</inbound-services>
// [END tracking_client_connections_and_disconnections]

// [START channel_presence]
// In the handler for _ah/channel/connected/
ChannelService channelService = ChannelServiceFactory.getChannelService();
ChannelPresence presence = channelService.parsePresence(req);
// [END channel_presence]
