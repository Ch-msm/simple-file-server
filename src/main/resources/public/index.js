const Y = {
  /**
   * api 路径
   */
  api: location.origin,
  /**
   * GET 方法
   * @param url {string} url路径
   * @param callback {Function} 回调函数
   */
  get(url, callback) {
    fetch(Y.api + '/' + url)
        .then(response => response.json())
        .then(callback)
        .catch(() => alert('加载失败'));
  },

  refreshFileList(url) {

  }
}