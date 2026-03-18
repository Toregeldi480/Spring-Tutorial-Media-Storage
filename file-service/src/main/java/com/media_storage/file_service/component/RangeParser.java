package com.media_storage.file_service.component;

import com.media_storage.file_service.exception.RangeNotSatisfiableException;
import org.springframework.stereotype.Component;

@Component
public class RangeParser {
    public Range parse(String rangeHeader, long fileSize) {
        if (rangeHeader == null || !rangeHeader.startsWith("bytes=")) return null;

        String rangeValue = rangeHeader.substring("bytes=".length()).trim();
        String[] parts = rangeValue.split("-");
        long start, end;

        if (rangeValue.startsWith("-")) {
            long suffixLength = Long.parseLong(parts[0]);
            start = Math.max(0, fileSize - suffixLength);
            end = fileSize - 1;
        } else if (rangeValue.endsWith("-")) {
            start = Long.parseLong(parts[0]);
            end = fileSize - 1;
        } else {
            start = Long.parseLong(parts[0]);
            end = Long.parseLong(parts[1]);
        }

        if (start > end) throw new RangeNotSatisfiableException("Requested Range Start Cannot Exceed Range End");
        if (start < 0) throw new RangeNotSatisfiableException("Requested Range Start Cannot Be Less Than 0");
        if (end >= fileSize) throw new RangeNotSatisfiableException("Requested Range End Cannot Exceed File Size");

        return new Range(start, end);
    }

    public static class Range {
        private long start;
        private long end;

        public Range(long start, long end) {
            this.start = start;
            this.end = end;
        }

        public long getContentLength() {
            return end - start + 1;
        }
        public long getStart() {
            return this.start;
        }

        public long getEnd() {
            return this.end;
        }
    }

}
