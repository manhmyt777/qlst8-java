<%@ page pageEncoding="UTF-8" %>
<%@ include file="Header8.jspf" %>
<p class="eyebrow">TỔNG QUAN</p><h1>Xin chào, <%=Html8.e(me.getFullName())%></h1><p class="muted">Một nơi để theo dõi hàng hóa và vận hành cửa hàng.</p>
<div class="hero"><div><span class="pill">ĐIỆN MÁY · QLST8</span><h2>Mỗi giao dịch,<br>đều trong tầm kiểm soát.</h2><p>Chọn chức năng ở thanh bên để bắt đầu phiên làm việc.</p></div><div class="hero-mark">08</div></div>
<div class="metrics"><div class="metric"><span>Mặt hàng hiện có</span><strong><%=((List<?>)request.getAttribute("items")).size()%></strong></div><div class="metric"><span>Vai trò của bạn</span><strong class="role"><%=Html8.e(me.getRole())%></strong></div><div class="metric"><span>Ngày làm việc</span><strong class="role"><%=LocalDate.now()%></strong></div></div>
<%if(me.getRole().equals("DELIVERY")){%><div class="hint">Nhân viên giao hàng là tác nhân tiếp nhận hàng. Bộ thiết kế hiện tại chưa yêu cầu giao diện thao tác riêng cho vai trò này.</div><%}%>
<%@ include file="Footer8.jspf" %>
