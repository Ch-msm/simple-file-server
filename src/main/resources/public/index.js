const Y = {
  /**
   * api 路径
   */
  //api: location.origin,
  api: location.origin + '/api',
  /**
   * uuid
   * @returns {string}
   */
  getUuid: function () {
    return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      let r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  },
  /**
   *秒转天时分秒
   * @param {number} result
   */
  secondToDay(result) {
    let h = Math.floor(result / 3600) < 10 ? '0' + Math.floor(result / 3600) : Math.floor(result / 3600);
    let m = Math.floor((result / 60 % 60)) < 10 ? '0' + Math.floor((result / 60 % 60)) : Math.floor((result / 60 % 60));
    let s = Math.ceil((result % 60)) < 10 ? '0' + Math.ceil((result % 60)) : Math.ceil((result % 60));
    return h + ":" + m + ":" + s;
  },
  /**
   * 全局封装
   * @param input {RequestInfo}
   * @param [init] {RequestInit}
   */
  fetch(input, init) {
    return fetch(input, init).then(response => {
      if (response.status >= 200 && response.status < 300) {
        return response;
      }
      const error = new Error(response.statusText);
      error.response = response;
      throw error;
    });

  },
  /**
   * GET 方法
   * @param url {string} url路径
   * @param callback {Function} 回调函数
   */
  get(url, callback) {
    this.fetch(Y.api + '/' + url)
        .then(response => response.json())
        .then(callback)
        .catch(e => alert('加载失败:' + e.message));
  },
  /**
   *文件上传
   * @param fileBean java对象
   * @param callback
   * @returns {Promise<String>}
   */
  upLoad(fileBean, callback) {
    let formData = new FormData();
    for (let k of Object.keys(fileBean)) {
      formData.set(k, fileBean[k])
    }
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest();
      let startTime = new Date().getTime()
      xhr.open('post', Y.api + '/upload');
      xhr.onload = e => resolve(e.target['responseText']);
      xhr.onerror = reject;
      xhr.upload.onprogress = ev => {
        //上传
        let loaded = ev.loaded, total = ev.total;
        //所用秒数
        let second = (new Date().getTime() - startTime) / 1000;
        //速度
        let speed = loaded / second
        //剩余时间
        let remaining = (total - loaded) / speed;
        callback(((loaded / total) * 100).toFixed(1), Y.getFileSize(speed), Y.secondToDay(remaining));
      };
      xhr.send(formData);
    });
  },
  /**
   *下载
   * @param fileBean {object} java对象
   * @param callback {Function} 回调函数 返回进度
   */
  download(fileBean, callback) {
    return this.fetch(Y.api + '/file/' + fileBean.id).then(response => {
      let bytesReceived = 0, reader = response.body.getReader();
      let startTime = new Date().getTime();
      return new ReadableStream({
        start: controller => {
          reader.read().then(function process(result) {
            if (result.done) {
              controller.close();
              return;
            }
            controller.enqueue(result.value);
            bytesReceived += result.value.length;
            //所用秒数
            let second = (new Date().getTime() - startTime) / 1000;
            //速度
            let speed = bytesReceived / second
            //剩余时间
            let remaining = (fileBean.size - bytesReceived) / speed;
            callback(((bytesReceived / fileBean.size) * 100).toFixed(1), Y.getFileSize(speed), Y.secondToDay(remaining));
            return reader.read().then(process);
          });
        }
      });
    }).then(stream => new Response(stream))
        .then(response => response.blob())
        .then(blob => {
          let url = window.URL.createObjectURL(blob);
          let a = document.createElement('a');
          a.href = url;
          a.target = '_blank';
          a.style.display = 'none'
          document.body.appendChild(a)
          a.download = fileBean.name;
          a.click();
          window.URL.revokeObjectURL(url);
          document.body.removeChild(a)
          return new Promise(resolve => resolve())
        });
  },
  /**
   * 新建元素
   * @param html {string} 元素文本
   * @returns Node
   */
  $(html) {
    let template = document.createElement('template');
    html = html.trim();
    template.innerHTML = html;
    return template.content.firstChild;
  },
  /**
   *计算文件大小
   * @param fileByte {string|Number} 字节数
   * @returns {string} 文件大小 字符形式
   */
  getFileSize(fileByte) {
    let fileSizeByte = +fileByte;
    let fileSizeMsg = "";
    if (fileSizeByte < 1048576) fileSizeMsg = (fileSizeByte / 1024).toFixed(2) + "KB";
    else if (fileSizeByte === 1048576) fileSizeMsg = "1MB";
    else if (fileSizeByte > 1048576 && fileSizeByte < 1073741824) fileSizeMsg = (fileSizeByte / (1024 * 1024)).toFixed(2) + "MB";
    else if (fileSizeByte > 1048576 && fileSizeByte === 1073741824) fileSizeMsg = "1GB";
    else if (fileSizeByte > 1073741824 && fileSizeByte < 1099511627776) fileSizeMsg = (fileSizeByte / (1024 * 1024 * 1024)).toFixed(2) + "GB";
    else fileSizeMsg = "文件超过1TB";
    return fileSizeMsg;
  },
  /**
   *当前列表数据
   */
  listData: [],
  /**
   * 刷新文件列表
   * @param url {string} 资源路径
   *
   */
  refreshFileList(url = '') {
    document.querySelector('[data-name="资源路径"]').innerText = url;
    let tbody = document.querySelector('#file_list > tbody');
    tbody.innerHTML = '';
    if (url) {
      //如果url存在 显示上级目录
      let tr = Y.$(`
          <tr data-type="文件夹">
            <td><a data-name="文件名">../</a></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
            <td></td>
          </tr>`);
      let a = tr.querySelector('[data-name="文件名"]');
      a.onclick = () => {
        let array = url.split('/');
        array.pop();
        array.pop();
        Y.refreshFileList(array.join('/'));
      };
      tbody.appendChild(tr);
    }
    Y.get('list?url=' + url, data => {
      Y.listData = data;
      for (let o of data) {
        let tr = Y.getTr(o);
        tbody.appendChild(tr);
      }
    })
  },

  /**
   * tr赋值
   * @param o
   * @returns {Node}
   */
  getTr(o) {
    let fileName = o.name, fileSize = Y.getFileSize(o.size), time = o.time, fileType = o.type;
    let tr = Y.$(`
          <tr data-type="${fileType}"  data-id="${o.id}">
            <td>${fileName}</td>
            <td>${fileSize}</td>
            <td>${time}</td>
            <td>${fileType}</td>
            <td><div class="progress"><div data-progress="0%"></div></div></td>
            <td data-name="当前速度"></td>
            <td data-name="剩余时间"></td>
          </tr>`);
    tr.onclick = () => {
      if (tr.classList.contains('selected')) {
        tr.classList.remove('selected')
      } else {
        tr.classList.add('selected')
      }
    };
    return tr;
  },

  /**
   * 文件上传
   * @param files {FileList}
   */
  fileUpLoad(files) {
    let promises = [];
    let tbody = document.querySelector('#file_list > tbody');
    for (let file of files) {
      let fileBean = {
        id: Y.getUuid(),
        name: file.name,
        size: file.size,
        time: '',
        type: '',
        file: file,
        status: '正在上传'
      };
      let tr = Y.getTr(fileBean);
      tbody.appendChild(tr);
      Y.listData.push(fileBean);
      let progress = document.querySelector(`tr[data-id="${fileBean.id}"] div.progress>div`);
      let speed = document.querySelector(`tr[data-id="${fileBean.id}"]>td[data-name="当前速度"]`);
      let remaining = document.querySelector(`tr[data-id="${fileBean.id}"]>td[data-name="剩余时间"]`);
      promises.push(
          Y.upLoad(fileBean, (p, s, r) => {
            p = p + '%';
            progress.style.width = p;
            progress.setAttribute('data-progress', p);
            speed.innerText = s + '/s';
            remaining.innerText = r;
          })
      );
    }
    Promise.all(promises).then(() => {
      Y.refreshFileList();
    });
  },
  /**
   * 初始化页面
   */
  init() {
    let body = document.getElementById('body');
    //绑定拖拽事件
    body.ondrop = e => {
      e.stopPropagation();
      e.preventDefault();
      Y.fileUpLoad(e.dataTransfer.files)
    }
    Menu.init();
    Y.refreshFileList();
  }
}

/**
 *菜单
 */
const Menu = {
  /**
   * 右键当前元素
   */
  ele: null,
  /**
   * 获取当前选中数据(未选中时选择当前右键数据，无数据时返回空数组)
   * @returns {Array}
   */
  getSelected() {
    let ids = [];
    let nodeList = document.querySelectorAll('tr.selected');
    for (let node of nodeList) {
      ids.push(node.dataset['id']);
    }
    if (ids.length === 0) {
      //判断当前是否右键在td上
      let id = Menu.ele.parentElement.dataset['id'];
      if (id) {
        ids.push(id);
      }
    }
    return ids;
  },
  /**
   * 初始化菜单
   */
  init() {
    let body = document.getElementById('body');
    let menu = document.getElementById('menu');
    //绑定菜单事件
    body.addEventListener('contextmenu', evt => {
      Menu.ele = evt.target;
      /*获取当前鼠标右键按下后的位置，据此定义菜单显示的位置*/
      let right = body.clientWidth - evt.clientX;
      let bottom = body.clientHeight - evt.clientY;
      if (right < menu.offsetWidth) {
        menu.style.left = body.scrollLeft + evt.clientX - menu.offsetWidth + "px";
      } else {
        menu.style.left = body.scrollLeft + evt.clientX + "px";
      }
      if (bottom < menu.offsetHeight) {
        menu.style.top = body.scrollTop + evt.clientY - menu.offsetHeight + "px";
      } else {
        menu.style.top = body.scrollTop + evt.clientY + "px";
      }
      menu.style.visibility = "visible";
      evt.returnValue = false;
    });
    document.addEventListener('click', () => {
      menu.style.visibility = 'hidden';
    });
  },
  /**
   * 下载
   */
  download() {
    let selected = Menu.getSelected();
    if (selected.length === 0) {
      alert('请选择数据');
      return;
    }
    let data = Y.listData.filter(o => selected.includes(o.id));
    for (let obj of data) {
      if (obj.status === '下载完成') {
        let bool = confirm('是否重新下载:' + obj.name + '文件?');
        if (!bool) {
          continue;
        }
      }
      if (obj.status === '正在上传') {
        confirm('文件:' + obj.name + ' 正在上传,请稍后下载');
        continue;
      }
      let progress = document.querySelector(`tr[data-id="${obj.id}"] div.progress>div`);
      let speed = document.querySelector(`tr[data-id="${obj.id}"]>td[data-name="当前速度"]`);
      let remaining = document.querySelector(`tr[data-id="${obj.id}"]>td[data-name="剩余时间"]`);
      Y.download(obj, (p, s, r) => {
        if (p === '100.0') {
          p = '99.9';
        }
        p = p + '%';
        progress.style.width = p;
        progress.setAttribute('data-progress', p);
        speed.innerText = s + '/s';
        remaining.innerText = r;
      }).then(() => {
        progress.style.width = '100%';
        progress.setAttribute('data-progress', '100%');
        obj.status = '下载完成';
      });
    }
  },
  mkdir() {

  },
  delete() {
    let selected = Menu.getSelected();
    if (selected.length === 0) {
      alert('请选择数据');
      return;
    }
    if (confirm("删除当前文件和下级所有文件?")) {
      Y.get('delete?id=' + selected.join(','), () => {
        Y.refreshFileList();
      })
    }
  }
}

