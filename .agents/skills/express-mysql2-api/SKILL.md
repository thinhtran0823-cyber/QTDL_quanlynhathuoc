---
name: express-mysql2-api
description: Write Express 5 controllers and routes on Node.js using a mysql2/promise pool, parameterized queries, transactions and a {success, message, data} JSON response. Use for any task that mentions express, mysql2, controller, route, endpoint, REST API, nodejs or node.js backend files.
tags: [express, mysql2, nodejs, api]
---

# Controller + route Express với mysql2

## Quy ước chung
- Nhận pool: `const pool = require('../config/db');` (không sửa `db.js`, `server.js`).
- Mọi phản hồi: `res.status(code).json({ success, message, data })`. Thành công 200; lỗi người dùng/nghiệp vụ 400; không tìm thấy dữ liệu 404; lỗi hệ thống 500.
- Luôn truy vấn có tham số `?`; tuyệt đối không nối chuỗi từ `req.body`/`req.query`.
- Kiểm tra đầu vào trước khi chạm CSDL: `Number.isInteger`, mảng không rỗng, chuỗi không trống.
- Comment tiếng Việt, `async/await`, `try/catch` ở mọi handler.

## Route
- Xem `server.js` để biết tiền tố đã mount (ví dụ `/api/nhap-kho`); route chỉ khai báo phần đuôi (`router.get('/phieu-nhap', ...)`).
- Tên hàm export ở controller PHẢI khớp tên import ở route; cuối file `module.exports = { ... }`.

## Transaction
```js
const conn = await pool.getConnection();
try {
  await conn.beginTransaction();
  const [r] = await conn.query('INSERT INTO ... VALUES (?, ?)', [a, b]);
  // r.insertId dùng cho bảng con
  await conn.commit();
  return res.status(200).json({ success: true, message: '...', data: { id: r.insertId } });
} catch (error) {
  await conn.rollback();
  const loiNghiepVu = error.errno === 1644; // SIGNAL SQLSTATE '45000' từ trigger
  return res.status(loiNghiepVu ? 400 : 500)
            .json({ success: false, message: error.sqlMessage || error.message, data: null });
} finally {
  conn.release();
}
```
- Mọi truy vấn trong giao tác dùng `conn`, không dùng `pool`.
- Việc đã có trigger làm (cập nhật tồn kho, tổng tiền) thì KHÔNG làm lại bằng JS.

## Dữ liệu trả về
- Cột `DATE` mặc định thành chuỗi ISO có giờ; dùng `DATE_FORMAT(cot, '%Y-%m-%d')` trong SQL để trả `yyyy-MM-dd`.
- Cột `DECIMAL` của mysql2 trả về chuỗi: giữ nguyên.
- Danh sách cho client Java không có thư viện JSON: trả mảng PHẲNG (mỗi dòng một đối tượng, không lồng mảng).
- Tên bảng/cột đúng chữ hoa/thường như file schema; theo kiểu PascalCase của các controller hiện có.

## Tự rà trước khi xong
Không chạy `node`/`npm` nếu task không cho phép. Tự kiểm: `require` đúng đường dẫn, mọi `await` nằm trong hàm `async`, `conn.release()` luôn chạy, đủ dấu `}`/`)`.
