document.addEventListener('DOMContentLoaded', function() {
    // 获取所有可折叠容器
    const containers = document.querySelectorAll('.collapsible-container');
    
    containers.forEach(container => {
      const header = container.querySelector('.collapsible-header');
      const content = container.querySelector('.collapsible-content');
      const icon = header.querySelector('.icon');
      
      header.addEventListener('click', function() {
        // 检查内容当前是否展开
        const isExpanded = content.classList.contains('expanded');
        
        if (isExpanded) {
          // 收起内容
          content.style.maxHeight = '0px';
          content.style.paddingTop = '0';
          content.style.paddingBottom = '0';
          content.style.opacity = '0';
          header.classList.remove('active');
          setTimeout(() => {
            content.classList.remove('expanded');
          }, 300); // 等待动画完成
        } else {
          // 展开内容
          content.classList.add('expanded');
          // 在下一帧设置高度以触发过渡动画
          setTimeout(() => {
            content.style.maxHeight = content.scrollHeight + 'px';
            content.style.paddingTop = '15px';
            content.style.paddingBottom = '15px';
            content.style.opacity = '1';
          }, 10);
          header.classList.add('active');
        }
      });
      
      // 监听窗口大小变化，更新内容高度
      window.addEventListener('resize', function() {
        if (content.classList.contains('expanded')) {
          // 重新设置高度以适应内容变化
          content.style.maxHeight = '0px';
          setTimeout(() => {
            content.style.maxHeight = content.scrollHeight + 'px';
          }, 10);
        }
      });
    });
  });