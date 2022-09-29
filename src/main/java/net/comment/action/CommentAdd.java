package net.comment.action;

import java.io.IOException;
import java.rmi.ServerException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.board.action.Action;
import net.board.action.ActionForward;
import net.comment.db.Comment;
import net.comment.db.CommentDAO;

public class CommentAdd implements Action {
	
	@Override
	public ActionForward execute(HttpServletRequest request, HttpServletResponse response)
			throws ServerException, IOException {
		CommentDAO dao = new CommentDAO();
		
		Comment co = new Comment();
		co.setId(request.getParameter("id"));
		co.setContent(request.getParameter("content"));
		System.out.println("content=" + co.getContent());
		
		co.setComment_re_lev(Integer.parseInt(request.getParameter("comment_re_lev")));
		co.setComment_board_num(Integer.parseInt(request.getParameter("comment_board_num")));
		co.setComment_re_seq(Integer.parseInt(request.getParameter("comment_re_seq")));
		
		int ok = dao.commentsInsert(co);
		response.getWriter().print(ok);
		return null;
	}
}
