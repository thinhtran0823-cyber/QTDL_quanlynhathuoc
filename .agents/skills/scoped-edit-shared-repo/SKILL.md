---
name: scoped-edit-shared-repo
description: Edit only an allowed set of files in a shared team repository. Read the whitelist and blacklist first, keep changes minimal, never reformat or rename other people's files. Use for team or group projects, shared repo work, feature branches, allowed-file and forbidden-file lists, and any task that says not to touch other members' work.
tags: [scoped_edit, shared, team, repo]
---

# Chỉ sửa đúng phạm vi trong repo dùng chung

1. **Đọc luật trước**: mở `AGENTS.md`/`GEMINI.md` ở thư mục gốc, ghi lại danh sách "được phép" và "cấm". Không có luật rõ → chỉ tạo/sửa file mà task nêu đích danh.
2. **Kiểm tra từng lần ghi**: trước khi ghi một file, đối chiếu đường dẫn với danh sách được phép. Không nằm trong đó → dừng, không ghi.
3. **Sửa đúng một dòng** nghĩa là thay đúng dòng đó: giữ nguyên thụt lề, kiểu xuống dòng, mã hóa UTF-8, các dòng khác không đổi một ký tự (kể cả dòng lặp, dòng thừa, lỗi chính tả).
4. **Không** đổi tên, di chuyển, xóa file; không format lại; không "tiện tay" dọn code.
5. **Không đụng**: `.env*`, `.git`, `node_modules`, khóa phụ thuộc (`package.json`, `package-lock.json`), file schema gốc, tài liệu (docx, pptx, pdf, ảnh).
6. **Không** thêm thư viện, bảng, cột, endpoint ngoài phạm vi. Nếu cần → ghi đề xuất vào file ghi chú bàn giao mà luật cho phép.
7. **Thấy lỗi ở file của người khác** (route sai, tên bảng sai, thiếu tham số) → ghi vào ghi chú bàn giao, KHÔNG sửa.
8. **Không** chạy `git commit/checkout/push`; không tạo file rác (`.bak`, `.orig`, `tmp`, bản sao).
9. **Kiểm tra cuối task**: liệt kê file đã tạo/sửa và xác nhận từng file nằm trong danh sách được phép. Có file ngoài danh sách → hoàn tác phần đó.
