class Account < ActiveRecord::Base
  has_many :meshes

  attr_accessor :password_confirmation
  
  validates_presence_of :email, :password
  validates_format_of :email, :with => /\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\Z/i
  validates_uniqueness_of :email
  validates_confirmation_of :password
  
  before_save :hash_password
  
  def authenticate(password)
    self.password == Digest::SHA2.hexdigest(self.salt + password)
  end
  
  def clear_password
    self.salt = nil
    self.password = nil
    self.password_confirmation = nil
  end
  
  private
  
  def hash_password
    return if self.salt.present?
    
    self.salt = ActiveSupport::SecureRandom.base64(8)
    self.password = Digest::SHA2.hexdigest(self.salt + self.password) if self.password
    self.password_confirmation = Digest::SHA2.hexdigest(self.salt + self.password_confirmation) if self.password_confirmation
  end
end
