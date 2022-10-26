package com.board.projectboard.controller;


import com.board.projectboard.config.MD5Generator;
import com.board.projectboard.domain.constant.FormStatus;
import com.board.projectboard.domain.constant.SearchType;
import com.board.projectboard.dto.ArticleDto;
import com.board.projectboard.dto.FileDto;
import com.board.projectboard.dto.UserAccountDto;
import com.board.projectboard.dto.request.ArticleRequest;
import com.board.projectboard.dto.response.ArticleResponse;
import com.board.projectboard.dto.response.ArticleWithCommentsResponse;
import com.board.projectboard.dto.security.BoardPrincipal;
import com.board.projectboard.service.ArticleService;
import com.board.projectboard.service.FileService;
import com.board.projectboard.service.PaginationService;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/articles")
@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final PaginationService paginationService;
    private FileService fileService;


    @GetMapping
    public  String articles(
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        map.addAttribute("articles", articles);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchTypes", SearchType.values());

        return "articles/index";
    }

    @GetMapping("/{articleId}")
    public  String article(
            @PathVariable Long articleId,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map) {
        ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));
        Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        map.addAttribute("article", article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("totalCount", articleService.getArticleCount());

        return "articles/detail";
    }

    @GetMapping("/search-hashtag")
    public String searchArticleHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ){
        Page<ArticleResponse> articles = articleService.searchArticlesViaHashtag(searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());
        List<String> hashtags = articleService.getHashtags();

        map.addAttribute("articles", articles);
        map.addAttribute("hashtags", hashtags);
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchType", SearchType.HASHTAG);

        return "articles/search-hashtag";
    }

    @GetMapping("/form")
    public String articleForm(ModelMap map) {
        map.addAttribute("formStatus", FormStatus.CREATE);
        return "articles/form";
    }

    @PostMapping ("/form")
    public String postNewArticle(
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            ArticleRequest articleRequest
    ) {
        articleService.saveArticle(articleRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/articles";
    }

    @GetMapping("/{articleId}/form")
    public String updateArticleForm(@PathVariable Long articleId, ModelMap map) {
        ArticleResponse article = ArticleResponse.from(articleService.getArticle(articleId));

        map.addAttribute("article", article);
        map.addAttribute("formStatus", FormStatus.UPDATE);

        return "articles/form";
    }

    public String write(@RequestParam("file") MultipartFile files, ArticleDto articleDto) {
        try {
            String origFilename = files.getOriginalFilename();
            String filename = new MD5Generator(origFilename).toString();
            /* 실행되는 위치의 'files' 폴더에 파일이 저장됩니다. */
            String savePath = System.getProperty("user.dir") + "\\files";
            /* 파일이 저장되는 폴더가 없으면 폴더를 생성합니다. */
            if (!new File(savePath).exists()) {
                try{
                    new File(savePath).mkdir();
                }
                catch(Exception e){
                    e.getStackTrace();
                }
            }
            String filePath = savePath + "\\" + filename;
            files.transferTo(new File(filePath));

            FileDto fileDto = new FileDto();
            fileDto.setOrigFilename(origFilename);
            fileDto.setFilename(filename);
            fileDto.setFilePath(filePath);

            Long fileId = fileService.saveFile(fileDto);
            articleDto.fileId();
            articleService.saveArticle(articleDto);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }
    @PostMapping ("/{articleId}/form")
    public String updateArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            ArticleRequest articleRequest

    ) {
        articleService.updateArticle(articleId, articleRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/articles/" + articleId;
    }

    @PostMapping ("/{articleId}/delete")
    public String deleteArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
            ) {
        articleService.deleteArticle(articleId, boardPrincipal.getUsername());

        return "redirect:/articles";
    }
    @RequestMapping(value="/singleImageUploader.do")
    public String simpleImageUploader(
            HttpServletRequest req, SmarteditorVO smarteditorVO, @PathVariable Long articleId)
            throws UnsupportedEncodingException{
        String callback = smarteditorVO.getCallback();
        String callback_func = smarteditorVO.getCallback_func();
        String file_result = "";
        String result ="";
        MultipartFile multiFile = smarteditorVO.getFiledata();
        try {
            if(multiFile != null && multiFile.getSize()>0 &&
                    StringUtils.isNotBlank(multiFile.getName())) {
                if (multiFile.getContentType().toLowerCase().startsWith("image/")) {
                    String oriName = multiFile.getName();
                    String uploadPath = req.getServletContext().getRealPath("/img");
                    String path = uploadPath + "/smarteditor/";
                    File file = new File(path);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    String fileName = UUID.randomUUID().toString();
                    smarteditorVO.getFiledata().transferTo(new File(path + fileName));
                    file_result += "&bNewLine=true&sFileName=" + oriName +
                            "&sFileURL=/img/smarteditor/" + fileName;
                } else {
                    file_result += "#errst=error";
                }
            }else{
                    file_result += "#errst=error";
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/articles/" + articleId;
    }

}
