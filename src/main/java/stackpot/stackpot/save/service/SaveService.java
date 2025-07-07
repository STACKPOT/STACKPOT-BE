package stackpot.stackpot.save.service;

import java.util.Map;

public interface SaveService {
    String feedSave(Long feedId);
    String potSave(Long feedId);
    Map<String, Object> getSavedPotsWithPaging(int page, int size);
    Map<String, Object> getSavedFeedsWithPaging(int page, int size);
}
