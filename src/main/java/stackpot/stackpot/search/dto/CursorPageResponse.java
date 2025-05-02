package stackpot.stackpot.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponse<T> {

    private List<T> content;   // 데이터 내용
    private Long nextCursor;  // 다음 페이지를 가져올 커서 값
    private boolean hasMore;  // 다음 데이터 존재 여부
}
