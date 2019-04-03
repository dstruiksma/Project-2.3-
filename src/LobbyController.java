
public class LobbyController extends AbstractController
{	
		private Connection connection;

		public LobbyController(Connection connection)
		{	
			LobbyView view = new LobbyView();
			view.setOnQuickPlayButtonPressHandler(this);
			view.setOnInviteButtonPressHandler(this);
			
			
			this.view = view;
			this.connection = connection;
			
			/*try {
				this.connection.connect();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		public void onQuickPlayButtonPress(String game, boolean isRegularPlayer)
		{
			if (game == null) {
				return;
			}
			
			System.out.println("Hoi " + game + " " + isRegularPlayer);
			// TODO: Stuur invite via Connection
		}
		
		public void onInviteButtonPress(String game, boolean isRegularPlayer, String invitePlayer)
		{
			if (game == null) {
				return;
			}
			
			System.out.println("Hoi " + invitePlayer + ", ik wil jou graag eem " + isRegularPlayer + " " + game);
			// TODO: Stuur invite via Connection
		}
		
		public String getTitle() {
			return "Lobby";
		}
}