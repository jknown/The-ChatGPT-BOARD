package com.example.board.controller;

import com.example.board.entity.Board;
import com.example.board.entity.Comment;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.CommentRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class BoardController {
    private final BoardRepository boardRepository;

    private final CommentRepository commentRepository;

    public BoardController(BoardRepository boardRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Board> boards = boardRepository.findAll();
        model.addAttribute("boards", boards);
        return "index";
    }

    @GetMapping("/form")
    public String createForm(Model model) {
        model.addAttribute("board", new Board());
        return "board/form";
    }

    @PostMapping("/board")
    public String create(@ModelAttribute Board board) {
        board.setCreatedAt(LocalDateTime.now());
        board.setViews(0);
        boardRepository.save(board);
        return "redirect:/";
    }

    @GetMapping("/board/{id}")
    public String view(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid board id: " + id));
        board.setViews(board.getViews() + 1);
        boardRepository.save(board);
        model.addAttribute("board", board);
        List<Comment> comments = commentRepository.order(id); // 해당 게시글의 댓글들을 order에 따라 정렬하여 가져옴
        model.addAttribute("comments", comments); // 댓글들을 모델에 추가
        return "board/detail";
    }

    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    public String createComment(@RequestParam Long postId,
                                @RequestParam(required = false) Long parentId,
                                @RequestParam String content) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setOrder("0");
        commentRepository.save(comment);
        return "redirect:/board/" + postId;
    }
}