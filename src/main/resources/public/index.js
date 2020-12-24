const Y = {
  api: location.origin,
  get(url, callback) {
    fetch(Y.api + '/' + url)
        .then(response => response.json())
        .then(callback)
        .catch(() => alert('加载失败'));
  }
}