Updated Network Protocol Documentation

'\*' indicates action types which are unverified (the game state is not
checksummed and compared after processing it).

Client -> Server Action Types
These are ONLY valid if sent from client to server.
 * Hello\* - The client requests to join a game on the server.
 	* Name - The player's name.
 	* Mods - A list of mod name & IDs being used by this client 
 	  (if applicable).
 * Play - The player has played a card
 	* Card - The type of the card played.
 	* Player - The ID of the player who played the card.
 * Start Draw - The player has chosen to begin drawing cards via the draw button.
 * Drew - The player drew a card via the draw button.
 * End Draw - The player has finished drawing cards via the draw button.
 * Resolve Challenge - The player, who had previously received the prompt to
   challenge a wild card, has chosen to do so or not to do so.
 	* Decision - Whether the player chose to challenge or decline.
 * Request Start - A client requests the server to begin the game and deal
   the cards. ONLY valid if sent by player 0 (the first one to join).
 	* Rule Set - The ruleset to use this game.
 * Send State\* - The client has encountered a desync and needs the server
   to send the current game state.

Server -> Client Action Types
These are ONLY valid if sent from server to client.
 * Welcome - A client has joined the game hosted on the server. Sent to all
   connected clients, including the one who just joined.
 	* Names - The names of all players currently in the game.
 	* Player - The ID of the player this action is being sent to.
 	* Players - The number of players currently in the game as of now.
 * Leave\* - A client has been rejected from joining the game or asked to leave.
   * Reason - A numeric ID of the reason the client has been asked to leave.
   * Message - The server-defined message explaining why they had to leave.
 * Start - Orders clients to start the game.
 	* State - The game state after starting and dealing.
 	* Seed - The RNG seed the client is to use for this game.
 * Set State - A desync has occurred and the server needs to ensure all clients
   have the same game state.
 	* State - The new state for the clients to set.
 	* Seed - The RNG seed the client is to use from now on.
 * Draw To Hand - A card is to be drawn into the hand of a particular player.
 	* Card - The type of card to be drawn.
 	* Target - The turn position of the player into whose hand the card was\
 	  drawn.
 * Remove from Hand - A card is to be removed from the hand of a particular
   player.
 	* Card - The type of the card to be removed.
    * Target - The turn position of the player from whose hand the card was
      removed.
 * Discard from Hand - A card was played by player and is now to be moved to
   the discard pile.
   	* Card - The type of the card to be discarded.
   	* Target - The turn position of the player who played (and discarded) the
   	  card.
 * Rotate Hands - All hands are to be rotated one space in the direction
   of the turn order.
 * Turn - A turn of Uno has been completed and it is time for the next player
   to play.
    * Card - The type of the last card played. This is needed because wild
      cards change their type between when they are played and when the turn
      advances.
 * Swap - 2 players are to swap hands.
    * Target1 - The turn position of the first player who is to swap hands.
    * Target2 - The turn position of the second player who is to swap hands.
 * Start Challenge - Begin a wild card challenge
 * End Challenge - End a running wildcard challenge.

Client <-> Server Types:
These types are valid when sent from either the client or the server.
 * Call Uno - A player has called "Uno" via the Uno button.
 * Keepalive - A short message to test whether the connection is still alive
   and the game state in sync.
 * Ack - Acknowledge the successful application of the received actions.
 * All plugin-defined types
 	* Plugins are responsible for determining whether an action is legal based
 	  on its origin.
 