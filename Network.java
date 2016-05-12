import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Class that will allow other players to join the game from remote places.
 * 
 * @author Bryce J. Fisher
 */

public class Network {

	// server variables
	volatile ArrayList<String> toSendQueue = new ArrayList<String>();
	volatile ArrayList<String> toRecieveQueue = new ArrayList<String>();
	volatile ArrayList<NamedSocket> serversListOfSocketsOfClients = new ArrayList<NamedSocket>();
	volatile ArrayList<Thread> clientThreads = new ArrayList<Thread>();
	ServerSocket server;
	volatile boolean wantsToKnowPLayers = false;

	// client variables
	volatile NamedSocket socketForClient = null;
	BufferedWriter writerForClient;
	BufferedReader readerForClient;
	volatile boolean connected = false;
	volatile boolean cleanClose = false;
	private final int retryMax = 3;

	// shared variables
	final boolean isServer;
	volatile boolean shutdown = false;
	private static InetAddress address;
	private final static int portNumber = 6112;
	private String name;
	volatile int idIndex = 0;
	volatile boolean hasStarted = false;

	// commands
	final static String CMD_PING = "PING";
	final static String CMD_START_GAME = "Start Game";
	final static String CMD_END_GAME = "End Game";
	final static String CMD_EXIT = "Exit";
	final static String CMD_SET_NAME = "SetName - ";
	final static String CMD_MESSAGE = "Message - ";
	final static String CMD_ASK_SERVER_FOR_PLAYERS = "GetPlayer";
	final static String CMD_SEND_PLAYERS_TO_CLIENTS = "List Players - ";
	final static String CMD_YOUR_ID = "INFO: Your ID - ";
	final static String CMD_SLAP = "Slap!";
	final static String CMD_TURN = "Turn is ";
	final static String CMD_PLAYED_CARD = "Play Card ";
	final static String CMD_REQUEST_PLAY_A_CARD = "Request a card";
	final static String CMD_CARD_COUNTS = "List Card Counts - ";
	final static String CMD_CLEAR_CARDS = "Clear Cards";
	final static String CMD_SYSTEM_MESSAGE = "SysMessage: ";
	final static String CMD_VICTORY = "Victory - ";

	/**
	 * Use this constructor only for testing
	 */
	public Network() {
		isServer = false;
	}

	/**
	 * Use this when hosting
	 * 
	 * @param hostingClientNumber
	 *            number of guests to join
	 */
	public Network(final int hostingClientNumber) {
		isServer = true;
		try {
			// get local address
			address = InetAddress.getLocalHost();
			// set name to start
			setName(address.toString() + ":" + portNumber);
			// debug string
			System.out.println("This program is hosting at "
					+ address.toString() + ":" + portNumber);
		} catch (final UnknownHostException e) {
			LobbyGUI.handleExceptionWithGUI(new RuntimeException(
					"Unable to get localhost address", e));
		}
		// Start the server thread
		final Thread serverThread = new Thread(new Runnable() {
			volatile NamedSocket currentSocket;

			@Override
			public void run() {

				try {
					// open up a server to listen for new connections
					server = new ServerSocket(portNumber);
					server.setSoTimeout(5000);
				} catch (final IOException e) {
					disconnect();
					LobbyGUI.handleExceptionWithGUI(new RuntimeException(
							"Unable to create server on port " + portNumber, e));
				}
				while (!shutdown) {
					try {

						// get each connection one at a time
						currentSocket = new NamedSocket(server.accept(),
								idIndex++);

						// add client to list
						serversListOfSocketsOfClients.add(currentSocket);

						// debug string
						System.err.println("User joined: "
								+ getSocketName(currentSocket));

						// create a new writer to talk to socket
						currentSocket.setBufferedWriter(new BufferedWriter(
								new OutputStreamWriter(currentSocket
										.getSocket().getOutputStream())));

						// create a new worker thread to listen to this socket
						final Thread clientListenerThread = new Thread(
								new Runnable() {
									@Override
									public void run() {
										// send user their id
										sendMessage(CMD_YOUR_ID);

										// assign the socket to this thread
										final NamedSocket sock = currentSocket;
										try {

											// create a reader to listen to this
											// socket
											final BufferedReader reader1 = new BufferedReader(
													new InputStreamReader(sock
															.getSocket()
															.getInputStream()));

											while (!shutdown) {
												// get each line one at a time
												String line = reader1
														.readLine();
												// if null, the socket has been
												// closed and the user
												// disconnected
												if (line != null) {
													// pings keep the connection
													// alive
													if (line.equals(CMD_PING)) {
														// do nothing
													} else if (line
															.equalsIgnoreCase(CMD_END_GAME)) {
														// this will shutdown
														// the server if a user
														// sends this command

														// debug string
														System.out.println("WARNING!!! Shutdown initiated by "
																+ getSocketName(sock)
																+ " - ID:"
																+ sock.getID());

														// inform other users
														// that the server is
														// shutting down
														toSendQueue
																.add(CMD_END_GAME);

														// wait five seconds
														// before shutdown
														for (int seconds = 5; seconds > 0; seconds--) {
															snooze(1000L);
															System.err
																	.println("Server shutdown in the next "
																			+ seconds
																			+ " seconds");

														}

														// close all connections
														disconnect();
													} else if (line
															.equalsIgnoreCase(CMD_EXIT)) {

														// this user is leaving,
														// close reader
														reader1.close();

														// debug string
														System.err.println("User disconnected: "
																+ getSocketName(sock)
																+ " - ID:"
																+ sock.getID());

														// close socket
														sock.getSocket()
																.close();

														// terminate thread
														return;
													} else if (line
															.startsWith(CMD_SET_NAME)) {
														// user requested name
														// change
														sock.setName(
																isServer,
																line.substring(CMD_SET_NAME
																		.length()));
													} else if (line
															.startsWith(CMD_MESSAGE)) {

														// user is sending a
														// chat message
														line = line
																.substring(CMD_MESSAGE
																		.length());

														// concatenate name and
														// message
														line = getSocketName(sock)
																+ " - " + line;
														line = CMD_MESSAGE
																+ line;

														// send message to all
														// clients
														toSendQueue.add(line);
													} else if (line
															.startsWith(CMD_ASK_SERVER_FOR_PLAYERS)) {
														// client wants to know
														// who else is online
														// create parsed string
														// of names and ids
														if (!hasStarted) {
															wantsToKnowPLayers = true;
														}
													} else if (line
															.startsWith(CMD_START_GAME)) {
														// host has
														// initiated
														// game start
														if (!hasStarted) {
															hasStarted = true;
															toSendQueue
																	.add(line);
														}
													} else if (line
															.startsWith(CMD_SLAP)
															|| line.startsWith(CMD_PLAYED_CARD)
															|| line.startsWith(CMD_TURN)
															|| line.startsWith(CMD_REQUEST_PLAY_A_CARD)
															|| line.startsWith(CMD_CARD_COUNTS)
															|| line.startsWith(CMD_CLEAR_CARDS)
															|| line.startsWith(CMD_SYSTEM_MESSAGE)
															|| line.startsWith(CMD_VICTORY)) {

														toSendQueue.add(line);
													} else {

														// a random non
														// protocol
														// string has
														// entered
														// the socket
														// streams,
														// output it and
														// ignore
														// it
														final String messageString = "Server - INVALID MESSAGE!!! - "
																+ getSocketName(sock)
																+ " - " + line;
														System.out
																.println(messageString);
														// TODO
													}
												} else {
													// shutdown has been
													// initiated, exit the

													// loop
													// and quite listening
													reader1.close();
													break;
												}

												snooze(10L);
											}
										} catch (final SocketException e) {
											// User disconnected without
											// informing the server
											System.err
													.println("User disconnected  (dirty): "
															+ getSocketName(sock)
															+ " - ID:"
															+ sock.getID()
															+ "\n\tReason: "
															+ e.getMessage());
										} catch (final IOException e) {
											// there was a problem when the user
											// tried to send something
											System.err
													.println("User failed to send command: "
															+ e.getMessage());
											e.printStackTrace();

										}

										try {
											// clean up and close the socket
											sock.getSocket().close();
										} catch (final IOException e) {
											LobbyGUI.handleExceptionWithGUI(e);
										}

									}
								});
						clientListenerThread
								.setName("Network - Server Listening Thread ID: "
										+ currentSocket.getID()
										+ " -- "
										+ getSocketName(currentSocket));
						clientListenerThread.setDaemon(true);
						clientListenerThread.start();
					} catch (final SocketTimeoutException e) {
						continue;
					} catch (final SocketException e) {
						// an exception is thrown when the server socket closes
						System.err.println("Server shutdown.");
					} catch (final IOException e) {
						// a user was unable to connect properly, likely an
						// issue with network
						System.err.println("User failed to connect: "
								+ e.getMessage());
						LobbyGUI.handleExceptionWithGUI(e);
					}
				}
				try {
					// clean up server before exiting thread
					server.close();
				} catch (final IOException e) {
					LobbyGUI.handleExceptionWithGUI(new RuntimeException(
							"Unable to close server on port " + portNumber, e));
				}
			}
		});
		serverThread.setName("Network - Server Connections Manager Thread");
		serverThread.start();

		// create a new thread to handle writing out to clients
		// the network acts as a hub in that it just repeats what ever it hears
		// to all clients
		// So there is no peer to peer
		final Thread serverWriterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!shutdown) {
					if (!toSendQueue.isEmpty()) {

						if (toSendQueue.size() > 50) {
							System.err.println("\ttoSendQueue.Size="
									+ toSendQueue.size());
						}

						// get the top item from the queue
						final String staticMessageToSend = toSendQueue
								.remove(0);

						if (hasStarted
								&& staticMessageToSend
										.startsWith(CMD_SEND_PLAYERS_TO_CLIENTS)) {
							continue;
						}

						// check for clients that have disconnected
						final ArrayList<NamedSocket> noLongerConnectedNeedToClose = new ArrayList<NamedSocket>();

						String dynamicMessageToSend;
						// write message to all clients
						for (final NamedSocket socketToWriteTo : serversListOfSocketsOfClients) {
							final BufferedWriter writer1 = socketToWriteTo
									.getBufferedWriter();
							dynamicMessageToSend = staticMessageToSend;
							if (staticMessageToSend.startsWith(CMD_YOUR_ID)) {
								dynamicMessageToSend = staticMessageToSend
										+ socketToWriteTo.getID();
							}
							try {
								writer1.write(dynamicMessageToSend);
								writer1.newLine();
								writer1.flush();
							} catch (final SocketException e) {
								// remove client if they disconnected
								noLongerConnectedNeedToClose
										.add(socketToWriteTo);
							} catch (final IOException e) {
								// debug string
								System.err.println("Unable to send message: "
										+ dynamicMessageToSend);
								noLongerConnectedNeedToClose
										.add(socketToWriteTo);
								e.printStackTrace();

							}
						}
						// remove clients if they disconnected
						for (final NamedSocket deadSocket : noLongerConnectedNeedToClose) {
							try {
								deadSocket.getBufferedWriter().close();
							} catch (final IOException e) {
								e.printStackTrace();
							}
							if (!deadSocket.getSocket().isClosed()) {
								try {
									deadSocket.getSocket().close();
								} catch (final IOException e) {
									e.printStackTrace();
								}
							}
							serversListOfSocketsOfClients.remove(deadSocket);
						}

					}
					snooze(10L);
				}
			}
		});
		serverWriterThread.setName("Network - Server Writer Thread");
		serverWriterThread.start();

		final Thread serverPlayerCounterIdManagerThread = new Thread(
				new Runnable() {

					@Override
					public void run() {
						while (!shutdown) {
							snooze(1000L);

							// prevent sending after game has started
							if (wantsToKnowPLayers && !hasStarted) {
								String toSend = "";
								for (final NamedSocket ns : serversListOfSocketsOfClients) {
									if (ns.getSocket().isClosed()) {
										continue;
									}
									toSend += ns.getID() + "::"
											+ StringParser.encode(ns.getName())
											+ ":::";
								}
								toSend = CMD_SEND_PLAYERS_TO_CLIENTS + toSend;

								// send to all players
								toSendQueue.add(toSend);
								wantsToKnowPLayers = false;
							}
						}
					}
				});
		serverPlayerCounterIdManagerThread.setDaemon(true);
		serverPlayerCounterIdManagerThread
				.setName("Network - Server Player Counter IdManager Thread");
		serverPlayerCounterIdManagerThread.start();
	}

	/**
	 * Use when joining a game
	 * 
	 * @param address
	 */
	public Network(final InetAddress address) {
		isServer = false;
		Network.address = address;

		// create new thread to connect to and listen to server
		final Thread listenerThreadForClient = new Thread(new Runnable() {
			@Override
			public void run() {
				int retryCount = 0;
				// client will attempt to reconnect if there is a slight
				// disruption
				while (!shutdown) {
					// user is attempting to disconnect cleanly, prevent rejoin
					if (cleanClose) {
						break;
					}
					System.err.println("Client - Attempting to connect to "
							+ getAddress().toString());
					if (!connected) {
						try {
							// init client variables
							socketForClient = new NamedSocket(new Socket(
									getAddress(), portNumber), idIndex++);

							writerForClient = new BufferedWriter(
									new OutputStreamWriter(socketForClient
											.getSocket().getOutputStream()));
							readerForClient = new BufferedReader(
									new InputStreamReader(socketForClient
											.getSocket().getInputStream()));
							connected = true;
							// reset retry count if server is now available
							retryCount = 0;
						} catch (final IOException e) {
							// if there is an issue create readers and writers,
							// terminate the connection
							disconnect();
							LobbyGUI.handleExceptionWithGUI(new RuntimeException(
									"Server not available:\n\t\t"
											+ "Unable to connect to "
											+ getAddress() + " on port "
											+ portNumber, e));
						}
					}
					while (!shutdown) {

						// user is attempting to disconnect cleanly, prevent
						// rejoin
						if (cleanClose) {
							break;
						}
						if (connected) {
							try {
								final String line = readerForClient.readLine();
								if (line == null) {
									// if line is null re-check if shutdown
									continue;
								}
								if (line.equals(CMD_END_GAME)) {
									System.err.println("Game Over!!!");
									disconnect();
									break;
								}
								toRecieveQueue.add(line);
							} catch (final IOException e) {
								connected = false;
								// prevent retries if network is in shutdown
								if (shutdown) {
									break;
								}

								System.err
										.println("Connection to Server disrupted: "
												+ e.getMessage()
												+ "\n\t@ "
												+ socketForClient.toString()
												+ "\nRetrying in 7 seconds ...");
								snooze(7000L);

								// shutdown = true;
								// e.printStackTrace();
							}
						} else {
							// if not connected, retry connection to server
							break;
						}
						snooze(10L);
					}
					retryCount++;
					// if server is unavailable, stop trying
					if (retryCount >= retryMax) {
						System.err
								.println("Maximum retries reached! Terminating network connection.");
						disconnect();
						break;
					}
				}
			}
		});
		listenerThreadForClient
				.setName("Network - Client Join/Listening Thread");
		listenerThreadForClient.start();

		final Thread writerThreadForClient = new Thread(new Runnable() {

			@Override
			public void run() {
				Long sysTime = System.currentTimeMillis();
				while (!shutdown) {

					if (connected) {
						try {
							if (!toSendQueue.isEmpty()) {
								final String toSend = toSendQueue.remove(0);
								if (toSend != null) {
									writerForClient.write(toSend);
								}
							} else {
								if (System.currentTimeMillis() > sysTime + 10000) {
									writerForClient.write(CMD_PING);
									sysTime = System.currentTimeMillis();
								} else {
									continue;
								}
							}
							writerForClient.newLine();
							writerForClient.flush();
						} catch (final IOException e) {
							connected = false;
							// LobbyGUI.handleExceptionWithGUI(e);
						}
					}
					snooze(10L);
				}
				try {
					socketForClient.getSocket().close();
				} catch (final Exception e) {
					System.err.println("shutdown = " + shutdown + " - "
							+ Thread.currentThread().getName());
					e.printStackTrace();
				}
			}
		});
		writerThreadForClient.setName("Network - Client Writer Thread");
		writerThreadForClient.start();

		final String nameString = address.toString() + ":" + portNumber;
		setName(nameString);
	}

	public static InetAddress getAddress() {
		return address;
	}

	public static int getPortNumber() {
		return portNumber;
	}

	public boolean isConnected() {
		if (isServer) {
			return !shutdown;
		}
		return connected;
	}

	public void sendMessage(final String message) {
		toSendQueue.add(message);
	}

	ArrayList<NetworkQueue> queues = new ArrayList<NetworkQueue>();

	/**
	 * 
	 * @return null if no message, the next message otherwise
	 */
	public String getMessage() {
		if (!isThereAMessage()) {
			return null;
		}
		final String message = toRecieveQueue.remove(0);
		for (final NetworkQueue q : queues) {
			q.addMessage(message);
		}
		return message;
	}

	public boolean isThereAMessage() {
		return !toRecieveQueue.isEmpty();
	}

	public void disconnect() {
		System.err.println("disconnect() called by "
				+ Thread.currentThread().getName());
		// if ordinary client, send "I'm leaving" command.
		if (!isServer) {
			cleanClose = true;
			sendMessage(CMD_EXIT);
			snooze(1000L);
		}
		connected = false;
		shutdown = true;
		if (isServer) {
			try {
				for (final NamedSocket sock : serversListOfSocketsOfClients) {
					sock.getSocket().close();
				}
				if (server != null) {
					server.close();
				}
			} catch (final IOException e) {
				LobbyGUI.handleExceptionWithGUI(e);

			}
		}

	}

	public static void snooze(final Long l) {
		try {
			Thread.sleep(l);
		} catch (final InterruptedException e) {
			LobbyGUI.handleExceptionWithGUI(e);
		}
	}

	protected static String getSocketName(final NamedSocket sock) {
		if (sock.getName() != null) {
			return sock.getName();
		}
		final String string = sock.getSocket().getLocalAddress() + ":"
				+ sock.getSocket().getLocalPort() + " - "
				+ sock.getSocket().getRemoteSocketAddress();
		sock.setName(true, string);
		return string;
	}

	public void setName(final String name) {
		this.name = name;
		if (!isServer) {
			if (socketForClient != null) {

				socketForClient.setName(isServer, name);
				sendMessage(CMD_SET_NAME + name);
			}
		}

	}

	public String getName() {
		if (isServer) {
			return name;
		}
		if (socketForClient != null) {
			return socketForClient.getName();
		}
		return name;
	}

	public NetworkQueue getNetworkQueue(final boolean isHost) {
		final NetworkQueue queue = new NetworkQueue(this, isHost);
		queues.add(queue);
		return queue;
	}

}

class NetworkQueue {

	private final Network network;
	private final boolean isHost;

	public NetworkQueue(final Network n, final boolean isHost) {
		network = n;
		this.isHost = isHost;

	}

	volatile ArrayList<String> toReceiveQueue2 = new ArrayList<String>();

	void sendMessageToQueue(final String message) {
		network.sendMessage(message);
	}

	void addMessage(final String message) {
		toReceiveQueue2.add(message);
	}

	String getMessageFromQueue() {
		if (toReceiveQueue2.isEmpty()) {
			return null;
		}
		return toReceiveQueue2.remove(0);
	}

	public boolean isHost() {
		return isHost;
	}
}

class NamedSocket {

	private String name = null;
	private final Socket socket;
	private final int ID;
	private BufferedWriter bufferedWriter;

	public NamedSocket(final Socket sock, final int id) {
		ID = id;
		socket = sock;
	}

	public String getName() {
		return name;
	}

	public void setName(final boolean isServer, final String name) {

		String previousName = getName();
		if (getName() == null) {
			previousName = "null";
		}
		if (previousName.equals(name)) {
			return;
		}
		this.name = name;
		System.err.println((isServer ? "Server - " : "Client - ")
				+ previousName + " - ID:" + getID() + " changed name to "
				+ this.name);
	}

	public Socket getSocket() {
		return socket;
	}

	@Override
	public String toString() {
		return getSocket().toString();
	}

	public int getID() {
		return ID;
	}

	public BufferedWriter getBufferedWriter() {
		return bufferedWriter;
	}

	public void setBufferedWriter(final BufferedWriter bufferedWriter) {
		this.bufferedWriter = bufferedWriter;
	}

}
