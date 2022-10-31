package com.board.projectboard.controller;


import com.board.projectboard.config.MD5Generator;
import com.board.projectboard.domain.constant.FormStatus;
import com.board.projectboard.domain.constant.SearchType;
import com.board.projectboard.dto.ArticleDto;
import com.board.projectboard.dto.FileDto;
import com.board.projectboard.dto.request.ArticleRequest;
import com.board.projectboard.dto.response.ArticleResponse;
import com.board.projectboard.dto.response.ArticleWithCommentsResponse;
import com.board.projectboard.dto.security.BoardPrincipal;
import com.board.projectboard.service.ArticleService;
import com.board.projectboard.service.FileService;
import com.board.projectboard.service.PaginationService;
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
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
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
    public String articles(
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
    public String article(
            @PathVariable Long articleId,
            @RequestParam(required = false) SearchType searchType,
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map) {
        ArticleWithCommentsResponse article = ArticleWithCommentsResponse.from(articleService.getArticleWithComments(articleId));
        Page<ArticleResponse> articles = articleService.searchArticles(searchType, searchValue, pageable).map(ArticleResponse::from);
        List<Integer> barNumbers = paginationService.getPaginationBarNumbers(pageable.getPageNumber(), articles.getTotalPages());

        map.addAttribute("article", article);
        map.addAttribute("articleComments", article.articleCommentsResponse());
        map.addAttribute("paginationBarNumbers", barNumbers);
        map.addAttribute("searchTypes", SearchType.values());
        map.addAttribute("totalCount", articleService.getArticleCount());

        return "articles/detail";
    }

    @GetMapping("/search-hashtag")
    public String searchArticleHashtag(
            @RequestParam(required = false) String searchValue,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            ModelMap map
    ) {
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

    @PostMapping("/form")
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
                try {
                    new File(savePath).mkdir();
                } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @PostMapping("/{articleId}/form")
    public String updateArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal,
            ArticleRequest articleRequest

    ) {
        articleService.updateArticle(articleId, articleRequest.toDto(boardPrincipal.toDto()));

        return "redirect:/articles/" + articleId;
    }

    @PostMapping("/{articleId}/delete")
    public String deleteArticle(
            @PathVariable Long articleId,
            @AuthenticationPrincipal BoardPrincipal boardPrincipal
    ) {
        articleService.deleteArticle(articleId, boardPrincipal.getUsername());

        return "redirect:/articles";
    }

    @RequestMapping(value = "/singleImageUploader.do")
    public String simpleImageUploader(
            HttpServletRequest request, SmarteditorVO smarteditorVO) throws UnsupportedEncodingException {
        String callback = smarteditorVO.getCallback();
        String callback_func = smarteditorVO.getCallback_func();
        String file_result = "";
        try {
            if (smarteditorVO.getFiledata() != null && smarteditorVO.getFiledata().getOriginalFilename() != null && !smarteditorVO.getFiledata().getOriginalFilename().equals("")) {
                //파일이 존재하면
                String original_name = smarteditorVO.getFiledata().getOriginalFilename();
                String ext = original_name.substring(original_name.lastIndexOf(".") + 1);
                //파일 기본경로
                String defaultPath = request.getSession().getServletContext().getRealPath("/");
                //파일 기본경로_상세경로
                String path = defaultPath + "resources" + File.separator + "upload" + File.separator;
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                //서버에 업로드할 파일명(한글문제로 인해 원본파일은 올리지 않는 것이 좋음
                String realname = UUID.randomUUID().toString() + "." + ext;
                //서버에 파일쓰기
                smarteditorVO.getFiledata().transferTo(new File(path + realname));
                file_result += "&bNewLine=true&sFileName=" + original_name + "&sFileURL=/resources/upload/" + realname;
            } else {
                file_result += "&errstr=error";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:" + callback + "?callback_func=" + URLEncoder.encode(callback_func, "UTF-8") + file_result;
    }

    @RequestMapping(value = "/multiplePhotoUpload.do")
    public void multiplePhotoUpload(
            HttpServletRequest request, HttpServletResponse response) {
        try {
            //파일정보
            String sFileInfo = "";
            //파일명을 받는다 - 일반 원본파일명
            String filename = request.getHeader("file-name");
            //파일 확장자
            String filename_ext = filename.substring(filename.lastIndexOf(".") + 1);
            //확장자를 소문자로 변경
            filename_ext = filename_ext.toLowerCase();
            //파일 기본경로
            String dftFilePath = request.getSession().getServletContext().getRealPath("/");
            //파일 기본경로_상세경로
            String filePath = dftFilePath + "resources" + File.separator + "upload" + File.separator;
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String realFileNm = "";
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String today = formatter.format(new java.util.Date());
            realFileNm = today + UUID.randomUUID().toString() + filename.substring(filename.lastIndexOf("."));
            String rlFileNm = filePath + realFileNm;

            InputStream is = request.getInputStream();
            OutputStream os = new FileOutputStream(rlFileNm);
            int numRead;
            byte b[] = new byte[Integer.parseInt(request.getHeader("file-size"))];
            while ((numRead = is.read(b, 0, b.length)) != -1) {
                os.write(b, 0, numRead);
            }
            if (is != null) {
                is.close();
            }
            os.flush();
            os.close();

            //서버에 파일쓰기
            //정보출력
            sFileInfo += "&bNewLine=true";
            sFileInfo += "&sFileName=" + filename;
            PrintWriter print = response.getWriter();
            print.print(sFileInfo);
            print.flush();
            print.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}