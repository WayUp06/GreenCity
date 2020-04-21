package greencity;

import greencity.dto.comment.AddCommentDto;
import greencity.dto.comment.CommentReturnDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.*;
import greencity.entity.enums.ROLE;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ModelUtils {
    public static Tag getTag() {
        return new Tag(1L, "tag", Collections.emptyList());
    }

    public static User getUser() {
        return User.builder()
            .id(1L)
            .email(TestConst.EMAIL)
            .name(TestConst.NAME)
            .role(ROLE.ROLE_USER)
            .lastVisit(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }

    public static EcoNewsAuthorDto getEcoNewsAuthorDto() {
        return new EcoNewsAuthorDto(1L, TestConst.NAME);
    }

    public static Language getLanguage() {
        return new Language(1L, "en", Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList());
    }

    public static EcoNews getEcoNews() {
        return new EcoNews(1L, ZonedDateTime.now(), TestConst.SITE, getUser(),
            "title", "text", Collections.singletonList(getTag()));
    }

    public static AddEcoNewsDtoRequest getAddEcoNewsDtoRequest() {
        return new AddEcoNewsDtoRequest("title", "text",
            Collections.singletonList("tag"));
    }

    public static AddEcoNewsDtoResponse getAddEcoNewsDtoResponse() {
        return new AddEcoNewsDtoResponse(1L, getEcoNews().getTitle(),
            getEcoNews().getText(), getEcoNewsAuthorDto(),
            getEcoNews().getCreationDate(), TestConst.SITE,
            Collections.singletonList("tag"));
    }

    public static MultipartFile getFile() {
        Path path = Paths.get("src/test/resources/test.jpg");
        String name = TestConst.IMG_NAME;
        String contentType = "photo/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
        }
        return new MockMultipartFile(name,
            name, contentType, content);
    }

    public static URL getUrl() throws MalformedURLException {
        return new URL(TestConst.SITE);
    }
    
    public static DiscountValue getDiscountValue() {
        return new DiscountValue(null, 33, null, null);
    }

    public static DiscountValueDto getDiscountValueDto() {
        return new DiscountValueDto(33, null);
    }

    public static Comment getComment(){
        return new Comment(1L, "text", null,
                null, null, null, Collections.emptyList(), null, null, null);
    }

    public static CommentReturnDto getCommentReturnDto(){
        return new CommentReturnDto(1L, "text", null, null, null);
    }

    public static Place getPlace(){
        return new Place(1L, "name", "description", null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null );
    }

    public static AddCommentDto getAddCommentDto(){
        return new AddCommentDto("comment", null, null);
    }


}
