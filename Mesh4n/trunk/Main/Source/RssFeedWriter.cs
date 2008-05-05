using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Globalization;
using System.Text.RegularExpressions;

namespace Mesh4n
{
	public class RssFeedWriter : FeedWriter
	{
		// simplified to avoid exponencial check time against device guids.
		private Regex emailExp = new Regex(@"[a-z0-9+_-]+@[a-z0-9][a-z0-9-]*(\.[a-z0-9-]+)+", RegexOptions.Compiled | RegexOptions.ExplicitCapture | RegexOptions.IgnoreCase);
		// we're more restrictive to be on the safe side.
		private Regex validCharExp = new Regex("[a-z0-9-]", RegexOptions.Compiled | RegexOptions.ExplicitCapture | RegexOptions.IgnoreCase);

		public RssFeedWriter(XmlWriter writer) : base(writer) { }

		protected override void WriteStartFeed(Feed feed, XmlWriter writer)
		{
			writer.WriteStartElement("rss");
			writer.WriteAttributeString("version", "2.0");
			writer.WriteStartElement("channel");
			if(!String.IsNullOrEmpty(feed.Title))
				writer.WriteElementString("title", feed.Title);
			
			if(!String.IsNullOrEmpty(feed.Description))
				writer.WriteElementString("description", feed.Description);

			if(!String.IsNullOrEmpty(feed.Link))
				writer.WriteElementString("link", feed.Link);

			if (feed.Payload != null)
			{
				foreach (XmlElement el in feed.Payload.ChildNodes)
				{
					writer.WriteNode(new XmlNodeReader(el), false);
				}
			}
		}

		protected override void WriteEndFeed(Feed feed, XmlWriter writer)
		{
			writer.WriteEndElement();
			writer.WriteEndElement();
		}

		protected override void WriteStartItem(Item item, XmlWriter writer)
		{
			writer.WriteStartElement("item");
			if (!item.Sync.Deleted)
			{
				writer.WriteElementString("title", item.XmlItem.Title);
				writer.WriteElementString("description", item.XmlItem.Description);
				foreach (XmlNode child in item.XmlItem.Payload.ChildNodes)
				{
					if (!((child.LocalName == "title" && child.NamespaceURI == "") ||
						(child.LocalName == "description" && child.NamespaceURI == "") ||
						(child.LocalName == "author" && child.NamespaceURI == "")))
					{
						writer.WriteNode(new XmlNodeReader(child), false);
					}
				}
			}
			else
			{
				writer.WriteElementString("title", String.Format(
					CultureInfo.CurrentCulture, 
					Properties.Resources.DeletedTitle, 
					item.Sync.LastUpdate.When.Value.ToShortDateString(), 
					item.Sync.LastUpdate.By));
			}

			if (item.Sync.LastUpdate != null &&
				item.Sync.LastUpdate.By != null)
			{
				string by = item.Sync.LastUpdate.By;

				if (emailExp.IsMatch(by))
				{
					writer.WriteElementString("author", item.Sync.LastUpdate.By);
				}
				else
				{
					WriteGeneratedAuthor(writer, by);
				}
			}
			else
			{
				// Default to email build from device author.
				WriteGeneratedAuthor(writer, DeviceAuthor.Current);
			}
		}

		private void WriteGeneratedAuthor(XmlWriter writer, string by)
		{
			if (by.Split('\\').Length == 2)
			{
				// Default format generated by our own library.
				string[] values = by.Split('\\');
				writer.WriteElementString("author",
					values[1].Trim() + "@" + values[0].Trim() + ".com");
			}
			else
			{
				// Filter out invalid email chars.
				StringBuilder email = new StringBuilder();
				foreach (char c in by)
				{
					if (validCharExp.IsMatch(c.ToString()))
						email.Append(c);
				}

				writer.WriteElementString("author",
					email.ToString() + "@" + "example.com");
			}
		}

		protected override void WriteEndItem(Item item, XmlWriter writer)
		{
			writer.WriteEndElement();
		}
	}
}
