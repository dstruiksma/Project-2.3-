package Framework;

import Interface.AbstractController;
import Interface.Popup;
import Interface.Router;

import Framework.AbstractPlayer;

import Connection.Events.*;
import Connection.Connection;

import javafx.application.Platform;

/**
 * AbstractGameController forms the basis for games. Games are expected to extent it.
 * This class provides generic functionality for turn based board games, and handles
 * the connection to the server and the event that come from it.
 */
public abstract class AbstractGameController extends AbstractController implements EventHandler
{
	protected Board board;

	protected AbstractPlayer player1, player2;

	protected boolean turn = false;

	protected int turnSwitches = 0;

	protected PlayController parentController;

	protected Connection connection;

	protected AbstractGameView view;

	private EventHandler handler;

	/**
	 * AbstractGameController initialises the game.
	 *
	 * @param connection
	 * @param p1
	 * @param p2
	 * @param parent Parent PlayController.
	 * @param startTurn Which player has the first turn. True for p2.
	 */
	public AbstractGameController(Connection connection, AbstractPlayer p1,
			AbstractPlayer p2, PlayController parent, boolean startTurn)
	{
		this.connection = connection;
		this.player1 = p1;
		this.player2 = p2;
		this.parentController = parent;

		this.turn = startTurn;

		if (this.isRemoteGame() != null) {
			this.registerEventHandlers();
		}

		BotPlayer bot = this.isBotGame();
		if (bot != null && !startTurn) {
			// (If it's our turn, and we're a bot:
			//   it could be that we're not getting the YourMoveEvent
			//   because raceyness, but it can be that we also will get
			//   it. Wait a bit to check if a move has been made,
			//   if not, then we likely missed the YourMoveEvent)

			boolean botTurn = startTurn;
			(new Thread(() -> {
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					return;
				}

				Platform.runLater(() -> {
					// If it's still our turn.
					if (!this.turn && this.turnSwitches == 0) {
						System.out.println("Manually make botmove");
						this.makeBotMove(botTurn, bot);
					}
				});

			})).start();
		}
	}

	private void registerEventHandlers()
	{
		this.connection.register(this);
	}

	/**
	 * handleEvent satisfies EventHandler
	 *
	 * @param event
	 */
	public void handleEvent(Event event)
	{
		Platform.runLater(() -> {
			if (event instanceof TurnEvent) {
				TurnEvent e = (TurnEvent) event;

				int[] xy = this.board.moveToXY(e.move);
				
				if (e.player.equals(this.player1.getName())) {
					// This is us.
				} else if (e.player.equals(this.player2.getName())) {
					this.makeServerMove(true, xy[0], xy[1]);
				} else {
					try {
						throw new Exception("not p1 and not p2? wat? " + e.player + ", " + this.player1.getName() + ", " + this.player2.getName());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if (event instanceof YourMoveEvent) {
				if (this.player1 instanceof LocalPlayer) {
					System.out.println("YourMove 1");
					if (!this.turn) {
						System.out.println("ALREADY MUH MOVE");
						return;
					}
					this.switchTurn(false);
				} else if (this.player2 instanceof LocalPlayer) {
					// This is remote.
					System.out.println("YourMove");
				} else {
					System.out.println("this shouldn't happen m8");
				}
			} else if (event instanceof MatchWonEvent) {
				System.out.println("You won The game!");

				Popup popup = new Popup("Win", "You won The Game!");
				popup.onClose(eve -> {
					this.quit();
				});
				popup.show();
			} else if (event instanceof MatchTiedEvent) {
				System.out.println("You tied The Game!");
			} else if (event instanceof MatchLostEvent) {
				MatchLostEvent e = (MatchLostEvent)event;

				System.out.println("You lost The Game!");

				Popup popup = new Popup("Lost", "You lost The Game!");
				popup.onClose(eve -> {
					this.quit();
				});
				popup.show();
		
			}
		});
	}

	abstract protected void makeServerMove(boolean turn, int x, int y);

	/**
	 * switchTurn changes the current player allowed to make a move in the game.
	 *
	 * @param newTurn Player to switch turns to. True for player2.
	 */
	protected void switchTurn(boolean newTurn)
	{
		this.turn = newTurn;

		this.turnSwitches++;

		AbstractPlayer playerTurn = (newTurn) ? this.player2 : this.player1;
		this.view.setMyTurn(playerTurn instanceof LocalPlayer);

		this.parentController.updateTurn(this.turn);

		if (!turn) {
			BotPlayer bot = this.isBotGame();
			if (bot != null) {
				Platform.runLater(() -> {
					this.makeBotMove(newTurn, bot);
				});
			}
		}
	}

	abstract protected void makeBotMove(boolean turn, BotPlayer bot);

	public AbstractGameView getView()
	{
		return this.view;
	}

	/**
	 * getPlayerAtMove returns the player that currently allowed to
	 * make a move in the game.
	 * This is either player1 or player2.
	 *
	 * @return AbstractPlayer
	 */
	public AbstractPlayer getPlayerAtMove()
	{
		if (this.turn) {
			return this.player2;
		}

		return this.player1;
	}

	/**
	 * quit cleans up this controller.
	 */
	public void quit()
	{
		this.connection.deRegister();

		Router.get().toLobby();
	}

	/**
	 * isBotGame returns the bot player in this game, if either player1 or
	 * player2 is a bot.
	 *
	 * @return BotPlayer
	 */
	public BotPlayer isBotGame()
	{
		if (this.player1 instanceof BotPlayer) {
			return (BotPlayer)this.player1;
		}

		if (this.player2 instanceof BotPlayer) {
			return (BotPlayer)this.player2;
		}

		return null;
	}

	/**
	 * isRemoteGame returns the remote player in this game, if either player1 or
	 * player2 is a remote player, which we play with by communicating with the
	 * server.
	 *
	 * @return RemotePlayer
	 */
	public RemotePlayer isRemoteGame()
	{
		if (this.player1 instanceof RemotePlayer) {
			return (RemotePlayer)this.player1;
		}

		if (this.player2 instanceof RemotePlayer) {
			return (RemotePlayer)this.player2;
		}

		return null;
	}
}
