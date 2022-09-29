package net.board.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class BoardDAO {
	private DataSource ds;
	
	public BoardDAO() {
		try {
			Context init = new InitialContext();
			ds = (DataSource) init.lookup("java:comp/env/jdbc/OracleDB");
		}catch (Exception ex) {
			System.out.println("DB 연결 실패 : " + ex);
		}
	}
	
	// 글 등록하기
	public boolean boardInsert(BoardBean board) {
		Connection con = null;
		PreparedStatement pstmt = null;
		int result=0;
		try {
			con = ds.getConnection();
			
			String max_sql = "(select nvl(max(board_num),0)+1 from board)";

			// 원문글의 BOARD_RE_REF 필드는 자신의 글번호 입니다.
			String sql = "insert into board " 
			            + "(BOARD_NUM,     BOARD_NAME,  BOARD_PASS,    BOARD_SUBJECT,"
					    + " BOARD_CONTENT, BOARD_FILE,  BOARD_RE_REF," 
			            + " BOARD_RE_LEV,  BOARD_RE_SEQ,BOARD_READCOUNT)"
					    + " values(" + max_sql + ",?,?,?," 
			            + "        ?,?," +   max_sql  + "," 
					    + "        ?,?,?)";

			// 새로운 글을 등록하는 부분입니다.
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, board.getBoard_name());
			pstmt.setString(2, board.getBoard_pass());
			pstmt.setString(3, board.getBoard_subject());
			pstmt.setString(4, board.getBoard_content());
			pstmt.setString(5, board.getBoard_file());
			
			// 원문의 경우 BOARD_RE_LEV, BOARD_RE_SEQ 필드 값은 0 입니다.
			pstmt.setInt(6, 0); // BOARD_RE_LEV 필드
			pstmt.setInt(7, 0); // BOARD_RE_SEQ 필드
			pstmt.setInt(8, 0); // BOARD_READCOUNT 필드
			
			result = pstmt.executeUpdate();
			if(result == 1 ) {
				System.out.println("데이터 삽입이 모두 완료되었습니다.");
				return true;
			}			
			
		}catch(Exception ex) {
			System.out.println("boardInsert() 에러: " + ex);
			ex.printStackTrace();
		}finally {
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally	
		return false;
	}// boardInsert
	
	//글의 갯수 구하기
	public int getListCount() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		int x = 0;
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement("select count(*) from board");
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				x = rs.getInt(1);
			}
			
		}catch(Exception ex) {
			System.out.println("getListCount() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally	
		return x;
	}// getListCount

	// 글 목록 보기
	public List<BoardBean> getBoardList(int page, int limit) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// page : 페이지
		// limit : 페이지 당 목록의 수
		// board_re_ref desc, board_re_seq asc에 의해 정렬한 것을
		// 조건절에 맞는 rnum의 범위 만큼 가져오는 쿼리문입니다.
		
		String board_list_sql = "select * "
							+ "  from (select rownum rnum, j.* "
						    + "  	   from ( select board.*, nvl(cnt,0) as cnt "
						   	+ "	  			  from board left outer join (select comment_board_num, count(*) cnt"
						   	+ "	  							  			  from comm"
						   	+ "	  							 			  group by comment_board_num)"
						   	+ "	  			  on board_num=comment_board_num"
						   	+ "	  			  order by BOARD_RE_REF desc,"
						   	+ "	  			  BOARD_RE_SEQ asc) j "
						   	+ "		   where rownum <= ? "
						   	+ "	       ) "
						   	+ "	where rnum >= ? and rnum <= ?";
		
		List<BoardBean> list = new ArrayList<BoardBean>();
		// 한 페이지당 10개씩 목록인 경우 1페이지, 2페이지, 3페이지, 4페이지 ...
		int startrow = (page - 1) * limit + 1; // 읽기 시작할 row 번호(1 11 21 31 ...
		int endrow = startrow + limit - 1;	   // 읽을 마지막 row 버호(10 20 30 40
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement(board_list_sql);
			pstmt.setInt(1, endrow);
			pstmt.setInt(2, startrow);
			pstmt.setInt(3, endrow);
			rs = pstmt.executeQuery();
			
			//DB에서 가져온 데이터를 VO객체에 담습니다.
			
			while (rs.next()) {
				BoardBean board = new BoardBean();
				board.setBoard_num(rs.getInt("BOARD_NUM"));
				board.setBoard_name(rs.getString("BOARD_NAME"));
				board.setBoard_subject(rs.getString("BOARD_SUBJECT"));
				board.setBoard_content(rs.getString("BOARD_CONTENT"));
				board.setBoard_file(rs.getString("BOARD_FILE"));
				board.setBoard_re_ref(rs.getInt("BOARD_RE_REF"));
				board.setBoard_re_lev(rs.getInt("BOARD_RE_LEV"));
				board.setBoard_re_seq(rs.getInt("BOARD_RE_SEQ"));
				board.setBoard_readcount(rs.getInt("BOARD_READCOUNT"));
				board.setBoard_date(rs.getString("BOARD_DATE"));
				board.setCnt(rs.getInt("cnt"));
				list.add(board); //값을 담은 객체를 리스트에 저장합니다.
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("getBoardList() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally	
		return list;
	}// getBoardList()메서드 end

	// 조회수 업데이트 - 글번호에 해당하는 조회수를 1 증가합니다.
	public void setReadCountUpdate(int num) {
		Connection con = null;
		PreparedStatement pstmt = null;
	      
		String sql = "update board "
				+ "set BOARD_READCOUNT=BOARD_READCOUNT+1 "
				+ "where BOARD_NUM = ?";
	      
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("setReadCountUpdate() 에러: " + ex);
		} finally {
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally  
	      
	}//getReadCount

	// 글 내용 보기
	public BoardBean getDetail(int num) {
		BoardBean board = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement("select * from board where BOARD_NUM = ?");
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				board = new BoardBean();
				board.setBoard_num(rs.getInt("BOARD_NUM"));
				board.setBoard_name(rs.getString("BOARD_NAME"));
				board.setBoard_subject(rs.getString("BOARD_SUBJECT"));
				board.setBoard_content(rs.getString("BOARD_CONTENT"));
				board.setBoard_file(rs.getString("BOARD_FILE"));
				board.setBoard_re_ref(rs.getInt("BOARD_RE_REF"));
				board.setBoard_re_lev(rs.getInt("BOARD_RE_LEV"));
				board.setBoard_re_seq(rs.getInt("BOARD_RE_SEQ"));
				board.setBoard_readcount(rs.getInt("BOARD_READCOUNT"));
				board.setBoard_date(rs.getString("BOARD_DATE"));
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("getDetail() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally	
		return board;
	}// getDetail()메서드 end

	public boolean isBoardWriter(int num, String pass) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean result = false;
		String board_sql = "select BOARD_PASS from board where BOARD_NUM=?";
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement(board_sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				if(pass.equals(rs.getString("BOARD_PASS"))) {
					result = true;
				}
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("isBoardWriter() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally	
		return result;
	}// isBoardWriter()메서드 end

	// 글 수정
	public boolean boardModify(BoardBean modifyboard) {
		Connection con = null;
		PreparedStatement pstmt = null;      
		String sql = "update board "
				+ "set BOARD_SUBJECT=?, BOARD_CONTENT=?, BOARD_FILE=? "
				+ "where BOARD_NUM = ? ";
	      
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, modifyboard.getBoard_subject());
			pstmt.setString(2, modifyboard.getBoard_content());
			pstmt.setString(3, modifyboard.getBoard_file());
			pstmt.setInt(4, modifyboard.getBoard_num());
			int result = pstmt.executeUpdate();
			if(result == 1) {
				System.out.println("성공 업데이트");
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("boardModify() 에러: " + ex);
		} finally {
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//finally  
	    return false;  
	}//boardModify() 메서드 end

	// 글 답변
	public int boardReply(BoardBean board) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int num = 0;
		
		// board 테이블의 글번호를 구하기 위해 board_num 컬럼의 최대값+1을 구해옵니다.
		String board_max_sql = "select max(board_num)+1 from board";	
		/*
		 * 답변을 할 원문 글 그룹 번호입니다.
		 * 답변을 달게 되면 답변 글은 이 번호와 같은 관련글 번호를 갖게 처리되면서
		 * 같은 그룹에 속하게 됩니다. 글 목록에서 보여줄때 하나의 그룹으로 묶어서 출력됩니다.
		 */
		int re_ref = board.getBoard_re_ref();	
		/*
		 * 답글의 깊이를 의미합니다.
		 * 원문에 대한 답글이 출력될 때 한 번 들여쓰기 처리가 되고 답글에 대한 답글은 들여쓰기가 두 번 처리되게
		 * 합니다. 원문인 경우에는 이 값이 0이고 원문의 답글은 1, 답글의 답글은 2가 됩니다.
		 */
		int re_lev = board.getBoard_re_lev();
		
		// 같은 관련 글 중에서 해당 글이 출력되는 순서입니다.
		int re_seq = board.getBoard_re_seq();
		
		try {
			con = ds.getConnection();
			
			// 트랜잭션을 이용하기 위해서 setAutoCommit을 false로 설정합니다.
			con.setAutoCommit(false);
			pstmt=con.prepareStatement(board_max_sql);
			rs=pstmt.executeQuery();
			if(rs.next()) {
				num=rs.getInt(1);
			}
			pstmt.close();	
			
			//BOARD_RE_REF, BOARD_RE_SEQ 값을 확인하여 원문 글에 다른 답글이 있으면
			//다른 답글들의 BOARD_RE_SEQ값을 1씩 증가시킵니다.
			//현재 글을 다른 답글보다 앞에 출력되게 하기 위해서 입니다.
			String sql = " update board " 
						+ "set    BOARD_RE_SEQ = BOARD_RE_SEQ + 1 " 
						+ "where  BOARD_RE_REF = ? "
						+ "and    BOARD_RE_SEQ > ?";
						
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, re_ref);
			pstmt.setInt(2, re_seq);
			pstmt.executeUpdate();
			pstmt.close();
			
			//등록할 답변 글의 BOARD_RE_LEV, BOARD_RE_SEQ 값을 원문 글보다 1씩 증가시킵니다.
			re_seq = re_seq + 1;
			re_lev = re_lev + 1;
			
			sql = "insert into board " 
				     + "(BOARD_NUM,BOARD_NAME,BOARD_PASS,BOARD_SUBJECT,"
					 + " BOARD_CONTENT, BOARD_FILE, BOARD_RE_REF," 
				     + " BOARD_RE_LEV, BOARD_RE_SEQ,BOARD_READCOUNT) " 
				     + " values(" + num + ","
				     + " ?,?,?,"
				     + " ?,?,?,"
				     + " ?,?,?)";
			
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, board.getBoard_name());
			pstmt.setString(2, board.getBoard_pass());
			pstmt.setString(3, board.getBoard_subject());
			pstmt.setString(4, board.getBoard_content());
			pstmt.setString(5, "");//답변에는 파일을 업로드하지 않습니다.
			pstmt.setInt(6, re_ref); //원문의 글번호
			pstmt.setInt(7, re_lev);
			pstmt.setInt(8, re_seq);
			pstmt.setInt(9, 0);//BOARD_READCOUNT(조회수)는 0
			if(pstmt.executeUpdate() == 1) {
				con.commit(); //commit합니다.
			}else {
				con.rollback();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("boardReply() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.setAutoCommit(true);
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}//execute end	
		return num;
	}//boardReply()메서드 end

	public boolean boardDelete(int num) {
		Connection con = null;
		PreparedStatement pstmt = null, pstmt2 = null;
		ResultSet rs = null;
		String select_sql = "select BOARD_RE_REF, BOARD_RE_LEV, BOARD_RE_SEQ "
						+ " from board"
						+ " where BOARD_NUM=?";
		
		String board_delete_sql = "delete from board "
	               + "          where  BOARD_RE_REF = ?"
	               + "          and    BOARD_RE_LEV >=?"
	               + "          and    BOARD_RE_SEQ >=?"
	               + "          and    BOARD_RE_SEQ <=("
	               + "                               nvl((SELECT min(board_re_seq)-1"
	               + "                                    FROM   BOARD "
	               + "                                    WHERE  BOARD_RE_REF=? "
	               + "                                    AND    BOARD_RE_LEV=? "
	               + "                                    AND    BOARD_RE_SEQ>?) , " 
	               + "                                    (SELECT max(board_re_seq) "
	               + "                                     FROM   BOARD "
	               + "                                     WHERE  BOARD_RE_REF=? ))"
	               + "                                 )";
		boolean result_check = false;
		try {
			con = ds.getConnection();
			pstmt = con.prepareStatement(select_sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				pstmt2 = con.prepareStatement(board_delete_sql);
				pstmt2.setInt(1, rs.getInt("BOARD_RE_REF"));
				pstmt2.setInt(2, rs.getInt("BOARD_RE_LEV"));
				pstmt2.setInt(3, rs.getInt("BOARD_RE_SEQ"));
				pstmt2.setInt(4, rs.getInt("BOARD_RE_REF"));
				pstmt2.setInt(5, rs.getInt("BOARD_RE_LEV"));
				pstmt2.setInt(6, rs.getInt("BOARD_RE_SEQ"));
				pstmt2.setInt(7, rs.getInt("BOARD_RE_REF"));
				
				int count=pstmt2.executeUpdate();
				
				if(count >= 1)
					result_check = true; // 삭제가 안된 경우에는 false를 반환합니다.
			}
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("boardDelete() 에러: " + ex);
		}finally {
			if(rs != null)
				try {
					rs.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt != null)
				try {
					pstmt.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(pstmt2 != null)
				try {
					pstmt2.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
			if(con != null)
				try {
					con.close();
				}catch (SQLException ex) {
					ex.printStackTrace();
				}
		}	
		return result_check;
	}// boardDelete()메서드 end

}

