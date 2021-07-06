package de.xehpuk.chessdiagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * @author xehpuk
 */
public class ChessDiagram {
	private static final String DELIMITER = "/";
	private static final int DIMENSION = 8;
	private static final int TILE_SIZE = 50;
	private static final int SIZE = DIMENSION * TILE_SIZE;
	private static final Color LIGHT = new Color(255, 206, 158);
	private static final Color DARK = new Color(209, 139, 71);
	
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(() -> {
			final JLabel board = new JLabel();
			board.setPreferredSize(new Dimension(SIZE, SIZE));
			final JTextField fenField = new JTextField();
			final JFrame window = new JFrame("Chess Diagram");
			fenField.addActionListener(ev -> {
				try {
					board.setIcon(new ImageIcon(fromFen(fenField.getText())));
				} catch (final IOException | IllegalArgumentException e) {
					final StringBuilder sb = new StringBuilder();
					for (Throwable ex = e; ex != null; ex = ex.getCause()) {
						sb.append(ex.getMessage()).append('\n');
					}
					JOptionPane.showMessageDialog(window, sb, "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
			final JPanel pane = new JPanel(new BorderLayout());
			pane.add(board, BorderLayout.CENTER);
			pane.add(fenField, BorderLayout.SOUTH);
			window.setContentPane(pane);
			window.setResizable(false);
			window.pack();
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setLocationRelativeTo(null);
			window.setVisible(true);
		});
	}
	
	public static Image fromFen(final String fen) throws IOException {
		final String[] ranks = fen.split(DELIMITER, Integer.MIN_VALUE);
		if (ranks.length < DIMENSION) {
			throw new IllegalArgumentException(String.format("Insufficient number of ranks (%d < %d)", ranks.length, DIMENSION));
		}
		if (ranks.length > DIMENSION) {
			throw new IllegalArgumentException(String.format("Too many ranks (%d > %d)", ranks.length, DIMENSION));
		}
		final Piece[][] p = new Piece[DIMENSION][];
		for (int i = 0; i < DIMENSION; i++) {
			try {
				p[i] = processRank(ranks[i]);
			} catch (final IllegalArgumentException iae) {
				throw new IllegalArgumentException(String.format("Problem at rank %d", DIMENSION - i), iae);
			}
		}
		return fromBoard(p);
	}

	private static Piece[] processRank(final String rank) {
		final Piece[] p = new Piece[DIMENSION];
		final int m = rank.length();
		int j = 0;
		for (int i = 0; i < m; i++) {
			if (j >= DIMENSION) {
				throw new IllegalArgumentException(String.format("End of rank but %d character(s) left (\"%s\")", m - i, rank.substring(i)));
			}
			final char c = rank.charAt(i);
			if (isDigit(c)) {
				int d = toDigit(c);
				if (isValidDigit(d, j)) {
					for (int k = 0; k < d; k++) {
						p[j++] = Piece.NONE;
					}
				} else {
					throw new IllegalArgumentException(String.format("Invalid digit %d at position %d (%d field(s) left)", d, i, DIMENSION - j));
				}
			} else {
				try {
					p[j++] = Piece.from(c);
				} catch (final IllegalArgumentException iae) {
					throw new IllegalArgumentException(String.format("Character at position %d", i), iae);
				}
			}
		}
		if (j < DIMENSION) {
			throw new IllegalArgumentException("End of input but rank incomplete");
		}
		return p;
	}

	private static boolean isDigit(final char c) {
		return '1' <= c && c <= '8';
	}

	private static int toDigit(final char c) {
		return c - '0';
	}

	private static boolean isValidDigit(final int d, final int j) {
		return d + j <= DIMENSION;
	}

	private static Image fromBoard(final Piece[][] p) throws IOException {
		final BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = img.createGraphics();
		try {
			for (int r = 0; r < DIMENSION; r++) {
				final Piece[] pr = p[r];
				for (int l = 0; l < DIMENSION; l++) {
					final Piece pl = pr[l];
					final File tileFile = new File(pl.toString());
					final Image tile = ImageIO.read(tileFile);
					g.drawImage(tile, l * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE, ((r + l) & 1) == 0 ? LIGHT : DARK, null);
				}
			}
		} finally {
			g.dispose();
		}
		return img;
	}

	private static enum Piece {
		WHITE_KING('K'),
		WHITE_QUEEN('Q'),
		WHITE_ROOK('R'),
		WHITE_BISHOP('B'),
		WHITE_KNIGHT('N'),
		WHITE_PAWN('P'),
		BLACK_KING('k'),
		BLACK_QUEEN('q'),
		BLACK_ROOK('r'),
		BLACK_BISHOP('b'),
		BLACK_KNIGHT('n'),
		BLACK_PAWN('p'),
		NONE((char) 0);
		
		private final char c;

		private Piece(final char c) {
			this.c = c;
		}
		
		public static Piece from(final char c) {
			for (final Piece p : values()) {
				if (p.c == c) {
					return p;
				}
			}
			throw new IllegalArgumentException(String.format("Unknown piece: %c", c));
		}
		
		@Override
		public String toString() {
			return "pics/" + this.name().toLowerCase() + ".png";
		}
	}
}
