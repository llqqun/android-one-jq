import './jsencrypt.js';
/**
 * RSA加密密码
 * @param {String} password 原始密码
 * @param {String} publicKey RSA公钥（可选，使用默认公钥）
 * @returns {Object} 包含加密结果的对象 {success: Boolean, encryptedPassword: String|null, error: String|null}
 */
export function encryptPassword(password, publicKey) {
  if (!password) {
    return { success: false, encryptedPassword: null, error: '密码不能为空' };
  }

  // 默认公钥
  var defaultPublicKey =
    '-----BEGIN PUBLIC KEY-----\n' +
    'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6N5zodp6n42GuKY/wIi5BiJVP\n' +
    '6IuWfUIK2kyve+MdsJ2qo7+Kqa2p+Nf3ojxfaxH9bQMYEB2Abo5BrMMIcQowMRHP\n' +
    '3ubmdO8DMGzVTN48CVOKs3zE4NUOenYgEXemk8UNIFk8oIhmuOA+4E7iA/th8aP9\n' +
    'Fu7yOie+NyWgUuiCKQIDAQAB\n' +
    '-----END PUBLIC KEY-----';

  var key = publicKey || defaultPublicKey;

  try {
    // 创建 JSEncrypt 实例
    var encrypt = new JSEncrypt();
    // 设置公钥
    encrypt.setPublicKey(key);
    // 对密码进行加密
    var encryptedPassword = encrypt.encrypt(password);

    // 判断是否加密成功
    if (encryptedPassword === false) {
      return { success: false, encryptedPassword: null, error: '密码加密失败' };
    }

    return { success: true, encryptedPassword: encryptedPassword, error: null };
  } catch (e) {
    return { success: false, encryptedPassword: null, error: '加密过程发生错误: ' + e.message };
  }
}
