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
package javaChessEngineToolkit;

import chesspresso.Chess;
import chesspresso.game.Game;
import chesspresso.move.Move;
import chesspresso.pgn.PGNWriter;
import chesspresso.position.Position;

import java.io.OutputStream;
import java.io.PrintWriter;

public class ChesspressoUtils {
	public static int getWhiteBoardStrength(Position position) {
		return getStrengthForColor(position, Chess.WHITE);
	}
	
	public static int getBlackBoardStrength(Position position) {
		return getStrengthForColor(position, Chess.BLACK);
	}
	
	private static int getStrengthForColor(Position position, int color) {
		int strength = 0;
		for (int sqi = 0; sqi < Chess.NUM_OF_SQUARES; sqi++) {
			int stone = position.getStone(sqi);
			if (Chess.stoneToColor(stone) == color) {
				int piece = position.getPiece(sqi);
				if (piece == Chess.KNIGHT) {
					strength += 3;
				} else if (piece == Chess.BISHOP) {
					strength += 3;
				} else if (piece == Chess.ROOK) {
					strength += 5;
				} else if (piece == Chess.QUEEN) {
					strength += 9;
				} else if (piece == Chess.PAWN) {
					strength += 1;
				}
			}
		}
		return strength;
	}
	
	public static boolean canWhiteCastleKingside(Position position) {
		return (position.getCastles() & Position.WHITE_SHORT_CASTLE) > 0;
	}
	
	public static boolean canWhiteCastleQueenside(Position position) {
		return (position.getCastles() & Position.WHITE_LONG_CASTLE) > 0;
	}
	
	public static boolean canBlackCastleKingside(Position position) {
		return (position.getCastles() & Position.BLACK_SHORT_CASTLE) > 0;
	}
	
	public static boolean canBlackCastleQueenside(Position position) {
		return (position.getCastles() & Position.BLACK_LONG_CASTLE) > 0;
	}
	
	public static int getDoublePawnPushFile(Position position) {
		//  -1 if the previous move was NOT a double pawn push, otherwise the chess 
		// board file  (numbered 0--7 for a--h) in which the double push was made
		
		if (position.getLastMove() == null) {
			return -1;
		}
		
		// was it a pawn move?
		if (position.getLastMove().getMovingPiece() == Chess.PAWN) {
			int fromSquare = position.getLastMove().getFromSqi();
			int toSquare = position.getLastMove().getToSqi();
			
			// was it a double pawn push?
			int delta = Chess.deltaCol(fromSquare, toSquare);
			if (Math.abs(delta) == 2) {
				// ok, now lets get the file idx.
				int file = Chess.sqiToCol(toSquare);
				return file;
			}
		}
		
		return -1;
	}
	
	public static String getVerboseNotation(Position position) {
		return getVerboseNotation(position, position.getLastShortMove());
	}
	
	public static String getVerboseNotation(Position position, short move) {
		if (position.getLastMove() == null) {
			return "none";
		}
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Chess.stoneToChar(position.getStone(Move.getFromSqi(move))));
		stringBuilder.append("/");
		stringBuilder.append(Chess.sqiToStr(Move.getFromSqi(move)).toLowerCase());
		stringBuilder.append("-");
		stringBuilder.append(Chess.sqiToStr(Move.getToSqi(move)).toLowerCase());
		if (Move.isPromotion(move)) {
			stringBuilder.append("=");
			stringBuilder.append(Chess.pieceToChar(Move.getPromotionPiece(move)));
		}
		return stringBuilder.toString();
	}
	
	public static boolean isDraw(Position position) {
		return hasInsufficientMatingMaterial(position) || position.getHalfMoveClock() >= 50;
	}
	
	public static boolean hasInsufficientMatingMaterial(Position position) {
		int whiteStrength = getStrengthForColor(position,Chess.WHITE);
		int blackStrength = getStrengthForColor(position,Chess.BLACK);
		return whiteStrength == 0 && blackStrength == 0;
	}
	
	public static void writePgn(Game game, OutputStream outputStream) {
		PrintWriter printWriter = new PrintWriter(outputStream);
		PGNWriter pgnWriter = new PGNWriter(printWriter);
		pgnWriter.write(game.getModel());
		printWriter.flush();
	}

	public static String getEPD(Game game,String bestMove,String id) {
		String fen = game.getPosition().getFEN();
		for(int i=0;i<2;i++) {
			fen = fen.substring(0,fen.lastIndexOf(" "));
		}
		return fen + " bm " + bestMove + "; id \"" + id + "\";";
	}
}
