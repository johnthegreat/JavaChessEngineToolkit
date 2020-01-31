/*
 * MIT License
 *
 * Copyright (c) 2020 John Nahlen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package javaChessEngineToolkit.engines;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.position.Position;
import javaChessEngineToolkit.ChesspressoUtils;
import javaChessEngineToolkit.Engine;
import javaChessEngineToolkit.GameContext;
import javaChessEngineToolkit.Move;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UciEngine extends Engine {
	protected static final Pattern bestMoveRegex = Pattern.compile("^bestmove (\\w{4,5})( ponder (.*))?$");

	protected String name;
	protected String uciEnginePath;
	protected Process process;
	protected BufferedReader bufferedReader;

	protected int estimatedElo;
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getEstimatedElo() {
		return this.estimatedElo;
	}

	private UciEngine() {

	}
	
	public UciEngine(String name,String uciEnginePath,int estimatedElo) throws IOException {
		this();
		this.name = name;
		this.uciEnginePath = uciEnginePath;
		this.estimatedElo = estimatedElo;
		final ProcessBuilder processBuilder = new ProcessBuilder().command(this.uciEnginePath);
		process = processBuilder.start();
		bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()),10000);
		sendUci();
		sendDebug(true);
	}

	private Move handleBestMoveLine(final Position position,String bestMoveLine) {
		Matcher matcher = bestMoveRegex.matcher(bestMoveLine);
		if (matcher.matches()) {
			String bestMove = matcher.group(1);
			String fromSqStr = bestMove.substring(0, 2);
			String toSqStr = bestMove.substring(2, 4);
			String promoPieceStr = null;
			if (bestMove.length() == 5) {
				promoPieceStr = bestMove.substring(4, 5);
			}

			int fromSqi = Chess.strToSqi(fromSqStr);
			int toSqi = Chess.strToSqi(toSqStr);
			int promoPiece = promoPieceStr == null ? Chess.NO_PIECE : Chess.charToPiece(promoPieceStr.toUpperCase().charAt(0));

			short shortValue = position.getMove(fromSqi, toSqi, promoPiece);

			Move move = new Move();
			move.setNotation(bestMove);
			move.setShortValue(shortValue);
			move.setScore(99.99);
			return move;
		}
		return null;
	}

	@Override
	public Future<Move[]> requestMove(final GameContext gameContext) {
		final CompletableFuture<Move[]> moveFuture = new CompletableFuture<Move[]>();
		final Position position = gameContext.getGame().getPosition();
		try {
			final Game gameCopy = new Game(gameContext.getGame().getModel());
			gameCopy.gotoStart();
			final chesspresso.move.Move[] mainLineMoves = gameCopy.getMainLine();
			String[] moves = new String[mainLineMoves.length];
			for(int i=0;i<mainLineMoves.length;i++) {
				final chesspresso.move.Move move = mainLineMoves[i];
				moves[i] = Chess.sqiToStr(move.getFromSqi()) + Chess.sqiToStr(move.getToSqi()) + (move.isPromotion() ? String.valueOf(Chess.pieceToChar(move.getPromo())).toLowerCase() : "");
			}
			sendPosition("startpos",moves);
			//sendPosition(position.getFEN(),null);

			if (gameContext.isTimed()) {
				// winc %d binc %d
				sendGo(String.format("wtime %d btime %d",gameContext.getChessClock().getClockForColor(Chess.WHITE),gameContext.getChessClock().getClockForColor(Chess.BLACK)));
			} else {
				sendGo("");
			}

			String line = null;
			while (null != (line = bufferedReader.readLine())) {
				if (line.startsWith("bestmove ")) {
					Move move = handleBestMoveLine(position,line);
					moveFuture.complete(new Move[] { move });
					break;
				} else if (line.startsWith("info ")) {
					// Optionally handle 'info' line
				} else if (line.startsWith("option ")) {
					// Optionally handle 'option' line
				} else if (line.equals("uciok")) {
					// Optionally handle 'uciok' line
				} else if (line.startsWith("id ")) {
					// Optionally handle 'id' line
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		return moveFuture;
	}

	@Override
	public void startNewGame(GameContext gameContext) {
		sendUciNewGame();
	}

	protected void write(final String line) {
		try {
			process.getOutputStream().write((line + "\n").getBytes());
			process.getOutputStream().flush();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void stop() {
		try {
			bufferedReader.close();
			process.destroy();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	public void sendUci() {
		write("uci");
	}

	public void sendDebug(boolean debug) {
		write("debug " + (debug?"on":"off"));
	}

	public void sendIsReady() {
		write("isready");
	}

	public void setOption(String id,String value) {
		if (value == null) {
			write(String.format("setoption name %s", id));
		} else {
			write(String.format("setoption name %s value %s", id, value));
		}
	}

	public void sendUciNewGame() {
		write("ucinewgame");
	}

	public void sendPosition(String fen,String[] moves) {
		StringBuilder stringBuilder = new StringBuilder();
		if (fen.equals("startpos")) {
			stringBuilder.append("position startpos");
		} else {
			stringBuilder.append("position fen ");
			stringBuilder.append(fen);
		}
		if (moves != null && moves.length > 0) {
			stringBuilder.append(" moves ");
			for (String move : moves) {
				stringBuilder.append(move);
				stringBuilder.append(" ");
			}
		}
		write(stringBuilder.toString().trim());
	}

	public void sendGo(String args) {
		if (args == null) {
			write("go");
		} else {
			write(("go " + args).trim());
		}
	}
}
